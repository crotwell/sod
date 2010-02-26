package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.omg.CORBA.BAD_PARAM;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.VirtualNetworkHelper;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.DBCacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.LazyNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.source.event.EventSource;
import edu.sc.seis.sod.source.network.NetworkFinder;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.networkArm.NetworkMonitor;
import edu.sc.seis.sod.subsetter.channel.ChannelEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.channel.PassChannel;
import edu.sc.seis.sod.subsetter.network.NetworkCode;
import edu.sc.seis.sod.subsetter.network.NetworkEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.network.NetworkOR;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;
import edu.sc.seis.sod.subsetter.network.PassNetwork;
import edu.sc.seis.sod.subsetter.station.PassStation;
import edu.sc.seis.sod.subsetter.station.StationEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

public class NetworkArm implements Arm {

    public NetworkArm(Element config) throws ConfigurationException {
        processConfig(config);
        refresh = new RefreshNetworkArm(this);
    }

    public void run() {
        try {
            SodDB sodDb = SodDB.getSingleton();
            // lastQueryTime should be null if first time
            lastQueryTime = sodDb.getQueryTime(getNetworkSource().getName(), getNetworkSource().getDNS());
            
            // only do timer if positive interval and waveform arm exists, otherwise run in thread
            if (getRefreshInterval().value > 0 && Start.getWaveformRecipe() != null) {
                Timer timer = new Timer("Refresh NetworkArm", true);
                long period = (long)getNetworkSource().getRefreshInterval().getValue(UnitImpl.MILLISECOND);
                long firstDelay = lastQueryTime==null ? 0 : lastQueryTime.delayUntilNextRefresh(getRefreshInterval());
                logger.debug("Refresh timer startup: period: "+period+"  firstDelay: "+firstDelay+"  last query: "+(lastQueryTime==null ? "null" : lastQueryTime.getTime()));
                timer.schedule(refresh, firstDelay, period);
            } else {
                // no refresh, so do net arm in this thread once
                refresh.run();
            }
        } catch(Throwable e) {
            armFinished = true;
            Start.armFailure(this, e);
        }
        // make sure any open read only db connetion is closed (ie from lastQueryTime above)
        SodDB.rollback();
    }

    public boolean isActive() {
        return !armFinished;
    }

    public String getName() {
        return "NetworkArm";
    }

    public CacheNetworkAccess getNetwork(NetworkId network_id)
            throws NetworkNotFound {
        List<CacheNetworkAccess> netDbs = getSuccessfulNetworks();
        MicroSecondDate beginTime = new MicroSecondDate(network_id.begin_time);
        String netCode = network_id.network_code;
        for (CacheNetworkAccess net : netDbs) {
            NetworkAttr attr = net.get_attributes();
            if(netCode.equals(attr.get_code())
                    && new MicroSecondTimeRange(attr.getEffectiveTime()).contains(beginTime)) {
                return net;
            }
        }
        throw new NetworkNotFound("No network for id: "
                + NetworkIdUtil.toString(network_id));
    }

    private void processConfig(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                loadConfigElement(SodUtil.load((Element)node, PACKAGES));
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
        getNetworkSource().setConstrainingNetworkCodes(getConstrainingNetworkCodes(attrSubsetter));
        configureEffectiveTimeCheckers();
    }

    private void configureEffectiveTimeCheckers() {
        EventArm arm = Start.getEventArm();
        if(arm != null && !Start.getRunProps().allowDeadNets()) {
            EventSource[] sources = arm.getSources();
            MicroSecondTimeRange fullTime = sources[0].getEventTimeRange();
            for(int i = 1; i < sources.length; i++) {
                fullTime = new MicroSecondTimeRange(fullTime,
                                                    sources[i].getEventTimeRange());
            }
            edu.iris.Fissures.TimeRange eventQueryTimes = fullTime.getFissuresTimeRange();
            netEffectiveSubsetter = new NetworkEffectiveTimeOverlap(eventQueryTimes);
            staEffectiveSubsetter = new StationEffectiveTimeOverlap(eventQueryTimes);
            chanEffectiveSubsetter = new ChannelEffectiveTimeOverlap(eventQueryTimes);
        } else {
            logger.debug("No implicit effective time constraint");
        }
    }

    public static final String[] PACKAGES = {"networkArm",
                                             "channel",
                                             "site",
                                             "station",
                                             "network"};

    private void loadConfigElement(Object sodElement)
            throws ConfigurationException {
        if(sodElement instanceof NetworkFinder) {
            finder = (NetworkFinder)sodElement;
        } else if(sodElement instanceof NetworkSubsetter) {
            attrSubsetter = (NetworkSubsetter)sodElement;
        } else if(sodElement instanceof StationSubsetter) {
            stationSubsetter = (StationSubsetter)sodElement;
        } else if(sodElement instanceof ChannelSubsetter) {
            chanSubsetters.add((ChannelSubsetter)sodElement);
        } else {
            throw new ConfigurationException("Unknown configuration object: "
                    + sodElement);
        }
    }

    public void add(NetworkMonitor monitor) {
        synchronized(statusMonitors) {
            statusMonitors.add(monitor);
        }
    }

    public ProxyNetworkDC getNetworkDC() {
        return getNetworkSource().getNetworkDC();
    }

    public TimeInterval getRefreshInterval() {
        return getNetworkSource().getRefreshInterval();
    }

    public List<ChannelSubsetter> getChannelSubsetters() {
        return chanSubsetters;
    }
    
    /**
     * returns an array of SuccessfulNetworks. if the refreshInterval is valid
     * it gets the networks from the database(may be embedded or external). if
     * not it gets the networks again from the network server specified in the
     * networkFinder. After obtaining the Networks if processes them using the
     * NetworkSubsetter and returns the successful networks as an array of
     * NetworkDbObjects.
     * 
     */
    public List<CacheNetworkAccess> getSuccessfulNetworks() {
        synchronized(refresh) {
            if(cacheNets == null) {
                setSuccessfulNetworks(loadNetworksFromDB());
            }
            while (cacheNets == null && lastQueryTime == null && ! Start.isArmFailure()) {
                // still null, maybe first time through
                logger.info("Waiting on initial network load");
                refresh.notifyAll();
                try {
                    refresh.wait(1000);
                } catch(InterruptedException e) {
                }
            }
            return cacheNets;
        }
    }
    
    void setSuccessfulNetworks(List<CacheNetworkAccess> nets) {
        this.cacheNets = Collections.unmodifiableList(nets);
    }
    
    List<CacheNetworkAccess> loadNetworksFromDB() {
        List<CacheNetworkAccess> out = getNetworkDB().getAllNets(getNetworkDC());
        for (CacheNetworkAccess net : out) {
            // this is for the side effect of creating
            // networkInfoTemplate stuff
            // avoids a null ptr later
            change(net,
                   Status.get(Stage.NETWORK_SUBSETTER,
                              Standing.SUCCESS));
        }
        return out;
    }

    List<CacheNetworkAccess> getSuccessfulNetworksFromServer() {
        synchronized(netGetSync) {
            statusChanged("Getting networks");
            logger.info("Getting networks");
            ArrayList<CacheNetworkAccess> successes = new ArrayList<CacheNetworkAccess>();
            List<CacheNetworkAccess> allNets;
            ProxyNetworkDC netDC = getNetworkDC();
            // purge cache before loading from server
            netDC.reset();
            synchronized(netDC) {
                allNets = getNetworkSource().getNetworks();
            }
            logger.info("Found " + allNets.size() + " networks");
            int i=0;
            for (CacheNetworkAccess cacheNetworkAccess : allNets) {
                CacheNetworkAccess currNet = new DBCacheNetworkAccess(cacheNetworkAccess);
                NetworkAttrImpl attr = null;
                try {
                    attr = (NetworkAttrImpl)currNet.get_attributes();
                    if(netEffectiveSubsetter.accept(attr).isSuccess()) {
                        StringTree result;
                        try {
                            result = attrSubsetter.accept(attr);
                        } catch(Throwable t) {
                            result = new Fail(attrSubsetter, "Exception", t);
                        }
                        if(result.isSuccess()) {
                            NetworkDB ndb = getNetworkDB();
                            int dbid = ndb.put(attr);
                            NetworkDB.commit();
                            logger.info("store network: " + attr.getDbid()
                                    + " " + dbid);
                            successes.add(currNet);
                            change(currNet,
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.SUCCESS));
                        } else {
                            change(currNet,
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.REJECT));
                            failLogger.info(NetworkIdUtil.toString(currNet.get_attributes()
                                    .get_id())
                                    + " was rejected.");
                        }
                    } else {
                        change(currNet, Status.get(Stage.NETWORK_SUBSETTER,
                                                      Standing.REJECT));
                        failLogger.info(NetworkIdUtil.toString(currNet.get_attributes()
                                .get_id())
                                + " was rejected because it wasn't active during the time range of requested events");
                    }
                } catch(Throwable th) {
                    GlobalExceptionHandler.handle("Got an exception while trying getSuccessfulNetworks for the "
                                                          + i
                                                          + "th networkAccess ("
                                                          +(attr==null?"null":NetworkIdUtil.toStringNoDates(attr)),
                                                  th);
                }
                i++;
            }
            if(lastQueryTime == null && allNets.size() == 0) {
                // hard to imagine a network arm that can't find a single good network is useful, so just fail
                // only fail if first time (lastQueryTime==null) so a server fail
                // during a run will not cause a crash
                logger.warn("Found no networks.  Make sure the network codes you entered are valid");
                Start.armFailure(this, new NotFound("Found no networks. Make sure the network codes you entered in your recipe are valid"));
            }
            logger.info(successes.size() + " networks passed");
            statusChanged("Waiting for a request");
            synchronized(refresh) {
                setSuccessfulNetworks(successes);
                refresh.notifyAll();
            }
            return successes;
        }
    }

    /**
     * Given a network subsetter, return a string array consisting of all the
     * network codes this subsetter accepts. If it doesn't constrain network
     * codes, an empty array is returned.
     */
    public static String[] getConstrainingNetworkCodes(NetworkSubsetter ns) {
        if(ns == null) {
            return new String[0];
        } else if(ns instanceof NetworkOR) {
            NetworkSubsetter[] kids = ((NetworkOR)ns).getSubsetters();
            String[] codes = new String[kids.length];
            for(int i = 0; i < kids.length; i++) {
                if(kids[i] instanceof NetworkCode) {
                    codes[i] = ((NetworkCode)kids[i]).getCode();
                } else {
                    return new String[0];
                }
            }
            return codes;
        } else if(ns instanceof NetworkCode) {
            return new String[] {((NetworkCode)ns).getCode()};
        } else {
            return new String[0];
        }
    }

    void finish() {
        armFinished = true;
        lastQueryTime = new QueryTime(getNetworkSource().getName(), getNetworkSource().getDNS(), ClockUtil.now().getTimestamp());
        SodDB.getSingleton().putQueryTime(lastQueryTime);
        SodDB.commit();
        logger.info("Network arm finished.");
        for (ArmListener listener : armListeners) {
            listener.finished(this);
        }
    }

    public void add(ArmListener listener) {
        armListeners.add(listener);
        if(armFinished == true) {
            listener.finished(this);
        }
    }

    /**
     * @return stations for the given network object that pass this arm's
     *         station subsetter
     */
    public StationImpl[] getSuccessfulStations(CacheNetworkAccess net) {
        return getSuccessfulStations(net.get_attributes());
    }

    public StationImpl[] getSuccessfulStations(NetworkAttrImpl net) {
        String netCode = net.get_code();
        logger.debug("getSuccessfulStations: "+net.get_code());
        synchronized(refresh) {
            while(refresh.isNetworkBeingReloaded(net.getDbid())) {
                try {
                    refresh.notifyAll();
                    refresh.wait();
                } catch(InterruptedException e) {}
            }
            if(allStationFailureNets.contains(NetworkIdUtil.toStringNoDates(net))) {
                return new StationImpl[0];
            }
            // try db
            List<StationImpl> sta = getNetworkDB().getStationForNet((NetworkAttrImpl)net);
            if(sta.size() != 0) {
                logger.debug("getSuccessfulStations " + netCode + " - from db "
                        + sta.size());
                return sta.toArray(new StationImpl[0]);
            } else {
                allStationFailureNets.add(NetworkIdUtil.toStringNoDates(net));
                return new StationImpl[0];
            }
        }
    }
    
    StationImpl[] getSuccessfulStationsFromServer(CacheNetworkAccess net) {
        synchronized(this) {
            statusChanged("Getting stations for "
                    + net.get_attributes().getName());
            ArrayList<Station> arrayList = new ArrayList<Station>();
            try {
                List<StationImpl> stations = getNetworkSource().getStations(net);
                for (StationImpl stationImpl : stations) {
                    logger.debug("Station in NetworkArm: "
                            + StationIdUtil.toString(stationImpl));
                }
                for (StationImpl currStation : stations) {
                    // hibernate gets angry if the network isn't the same object
                    // as the one already in the thread's session
                    currStation.setNetworkAttr(net.get_attributes());
                    StringTree effResult = staEffectiveSubsetter.accept(currStation,
                                                                        net);
                    if(effResult.isSuccess()) {
                        StringTree staResult;
                        try {
                            staResult = stationSubsetter.accept(currStation,
                                                                       net);
                        } catch(Throwable t) {
                            staResult = new Fail(stationSubsetter, "Exception", t);
                        }
                        if(staResult.isSuccess()) {
                            int dbid = getNetworkDB().put(currStation);
                            logger.info("Store " + currStation.get_code()
                                    + " as " + dbid + " in " + getNetworkDB());
                            arrayList.add(currStation);
                            change(currStation,
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.SUCCESS));
                        } else {
                            change(currStation,
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.REJECT));
                            failLogger.info(StationIdUtil.toString(currStation.get_id())
                                    + " was rejected: " + staResult);
                        }
                    } else {
                        change(currStation, Status.get(Stage.NETWORK_SUBSETTER,
                                                       Standing.REJECT));
                        failLogger.info(StationIdUtil.toString(currStation.get_id())
                                + " was rejected based on its effective time not matching the range of requested events: "
                                + effResult);
                    }
                }
                NetworkDB.commit();
            } catch(Exception e) {
                GlobalExceptionHandler.handle("Problem in method getSuccessfulStations for net "
                                                      + NetworkIdUtil.toString(net.get_attributes().get_id()),
                                              e);
                NetworkDB.rollback();
            }
            StationImpl[] rtnValues = new StationImpl[arrayList.size()];
            rtnValues = (StationImpl[])arrayList.toArray(rtnValues);
            statusChanged("Waiting for a request");
            logger.debug("getSuccessfulStations " + NetworkIdUtil.toStringNoDates(net.get_attributes()) + " - from server "
                    + rtnValues.length);
            if(rtnValues.length == 0) {
                allStationFailureNets.add(NetworkIdUtil.toStringNoDates(net.get_attributes()));
            } else {
                allStationFailureNets.remove(NetworkIdUtil.toStringNoDates(net.get_attributes()));
            }
            return rtnValues;
        }
    }

    /**
     * Obtains the Channels corresponding to the station, processes them using
     * the ChannelSubsetter and returns an array of those that pass
     * 
     * @throws NetworkNotFound
     */
    public List<ChannelImpl> getSuccessfulChannels(StationImpl station) {
        synchronized(refresh) {
            while(refresh.isNetworkBeingReloaded(((NetworkAttrImpl)station.getNetworkAttr()).getDbid())) {
                try {
                    refresh.notifyAll();
                    refresh.wait();
                } catch(InterruptedException e) {}
            }
            if(allChannelFailureStations.contains(StationIdUtil.toStringNoDates(station))) {
                return new ArrayList<ChannelImpl>(0);
            }
            // no dice, try db
            List<ChannelImpl> sta = getNetworkDB().getChannelsForStation(station);
            if(sta.size() != 0) {
                logger.debug("successfulChannels " + station.get_code()
                        + " - from db " + sta.size());
                return sta;
            } else {
                allChannelFailureStations.add(StationIdUtil.toStringNoDates(station));
                return new ArrayList<ChannelImpl>(0);
            }
        }
    }
    
    List<ChannelImpl> getSuccessfulChannelsFromServer(StationImpl station, CacheNetworkAccess networkAccess) {
        synchronized(this) {
            statusChanged("Getting channels for " + station);
            List<ChannelImpl> successes = new ArrayList<ChannelImpl>();
            try {               
                List<ChannelImpl> chansAtStation = getNetworkSource().getChannels(networkAccess, station);
                Status inProg = Status.get(Stage.NETWORK_SUBSETTER,
                                           Standing.IN_PROG);
                boolean needCommit = false;
                StationImpl dbSta = NetworkDB.getSingleton().getStation(station.getDbid());
                for (ChannelImpl chan : chansAtStation) {
                    // make the assumption that the station in the channel is the same as the station retrieved earlier
                    chan.getSite().setStation(dbSta);
                    change(chan, inProg);
                    StringTree effectiveTimeResult = chanEffectiveSubsetter.accept(chan,
                                                                                   networkAccess);
                    if(effectiveTimeResult.isSuccess()) {
                        boolean accepted = true;
                        synchronized(chanSubsetters) {
                            for (ChannelSubsetter cur : chanSubsetters) {
                                StringTree result;
                                try {
                                    result = cur.accept(chan,
                                                        networkAccess);
                                } catch(Throwable t) {
                                    result = new Fail(cur, "Exception", t);
                                }
                                if(!result.isSuccess()) {
                                    change(chan,
                                           Status.get(Stage.NETWORK_SUBSETTER,
                                                      Standing.REJECT));
                                    failLogger.info("Rejected "
                                            + ChannelIdUtil.toString(chan.get_id())
                                            + ": " + result);
                                    accepted = false;
                                    break;
                                }
                            }
                        }
                        if(accepted) {
                            getNetworkDB().put(chan);
                            logger.debug("Accept "+ChannelIdUtil.toString(chan.get_id()));
                            needCommit = true;
                            successes.add(chan);
                            change(chan, Status.get(Stage.NETWORK_SUBSETTER,
                                                    Standing.SUCCESS));
                        }
                    } else {
                        change(chan, Status.get(Stage.NETWORK_SUBSETTER,
                                                Standing.REJECT));
                        failLogger.info("Reject based on effective time not matching the range of requested events: "
                                + ChannelIdUtil.toString(chan.get_id())
                                + effectiveTimeResult);
                    }
                }
                if(needCommit) {
                    NetworkDB.commit();
                }
            } catch(Throwable e) {
                GlobalExceptionHandler.handle("Problem in method getSuccessfulChannels for "
                                                      + StationIdUtil.toString(station.get_id()),
                                              e);
                NetworkDB.rollback();
            }
            statusChanged("Waiting for a request");
            if(successes.size() == 0) {
                allChannelFailureStations.add(StationIdUtil.toStringNoDates(station));
            } else {
                allChannelFailureStations.remove(StationIdUtil.toStringNoDates(station));
            }
            return successes;
        }
    }

    public List<ChannelGroup> getSuccessfulChannelGroups(StationImpl station) {
        if(! refresh.isNetworkBeingReloaded(((NetworkAttrImpl)station.getNetworkAttr()).getDbid())
                && allChannelGroupFailureStations.contains(StationIdUtil.toStringNoDates(station))) {
            return new ArrayList<ChannelGroup>(0);
        }
        synchronized(refresh) {
            while(refresh.isNetworkBeingReloaded(((NetworkAttrImpl)station.getNetworkAttr()).getDbid())) {
                try {
                    refresh.notifyAll();
                    refresh.wait();
                } catch(InterruptedException e) {}
            }
            if(allChannelGroupFailureStations.contains(StationIdUtil.toStringNoDates(station))) {
                return new ArrayList<ChannelGroup>(0);
            }
            // no dice, try db
            List<ChannelGroup> sta = getNetworkDB().getChannelGroupsForStation(station);
            if(sta.size() != 0) {
                logger.debug("successfulChannelGroups " + station.get_code()
                        + " - from db " + sta.size());
                return sta;
            } else {
                allChannelGroupFailureStations.add(StationIdUtil.toStringNoDates(station));
                return new ArrayList<ChannelGroup>(0);
            }
        }
    }
    
    List<ChannelGroup> getSuccessfulChannelGroupsFromServer(StationImpl station, CacheNetworkAccess net) {
        synchronized(this) {
            List<ChannelImpl> failures = new ArrayList<ChannelImpl>();
            List<ChannelGroup> chanGroups = channelGrouper.group(getSuccessfulChannelsFromServer(station, net),
                                                             failures);
            for(ChannelGroup cg : chanGroups) {
                getNetworkDB().put(cg);
            }
            if (chanGroups.size() != 0) {
                NetworkDB.commit();
                allChannelGroupFailureStations.remove(StationIdUtil.toStringNoDates(station));
            } else {
                allChannelGroupFailureStations.add(StationIdUtil.toStringNoDates(station));
            }
            for (ChannelImpl failchan : failures) {
                failLogger.info(ChannelIdUtil.toString(failchan.get_id())
                        + "  Channel not grouped.");
            }
            return chanGroups;
        }
    }
    
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound {
        Instrumentation inst = getNetworkDB().getInstrumentation(chan);
        if (inst == null) {
            CacheNetworkAccess networkAccess = new LazyNetworkAccess((NetworkAttrImpl)chan.getNetworkAttr(),
                                                                     getNetworkDC());
            try {
                inst = networkAccess.retrieve_instrumentation(chan.getId(), chan.getBeginTime());
            } catch (ChannelNotFound e) {
                getNetworkDB().putInstrumentation(chan, null);
            }
            getNetworkDB().putInstrumentation(chan, inst);
        }
        return inst;
    }

    private void statusChanged(String newStatus) {
        synchronized(statusMonitors) {
            for (NetworkMonitor netMon : statusMonitors) {
                try {
                    netMon.setArmStatus(newStatus);
                } catch(Throwable e) {
                    // caught for one, but should continue with rest after
                    // logging
                    // it
                    GlobalExceptionHandler.handle("Problem changing status in NetworkArm",
                                                  e);
                }
            }
        }
    }

    private void change(Channel chan, Status newStatus) {
        synchronized(statusMonitors) {
            for (NetworkMonitor netMon : statusMonitors) {
                try {
                    netMon.change(chan, newStatus);
                } catch(Throwable e) {
                    // caught for one, but should continue with rest after
                    // logging
                    // it
                    GlobalExceptionHandler.handle("Problem changing channel status in NetworkArm",
                                                  e);
                }
            }
        }
    }

    private void change(Station sta, Status newStatus) {
        synchronized(statusMonitors) {
            for (NetworkMonitor netMon : statusMonitors) {
                try {
                    netMon.change(sta, newStatus);
                } catch(Throwable e) {
                    // caught for one, but should continue with rest after
                    // logging
                    // it
                    GlobalExceptionHandler.handle("Problem changing station status in NetworkArm",
                                                  e);
                }
            }
        }
    }

    private void change(NetworkAccess na, Status newStatus) {
        synchronized(statusMonitors) {
            for (NetworkMonitor netMon : statusMonitors) {
                try {
                    netMon.change(na, newStatus);
                } catch(Throwable e) {
                    // caught for one, but should continue with rest after
                    // logging
                    // it
                    GlobalExceptionHandler.handle("Problem changing network status in NetworkArm",
                                                  e);
                }
            }
        }
    }

    private NetworkFinder finder = new NetworkFinder("edu/iris/dmc", "IRIS_NetworkDC", -1);

    private NetworkSubsetter attrSubsetter = new PassNetwork();

    private NetworkSubsetter netEffectiveSubsetter = new PassNetwork();

    private StationSubsetter stationSubsetter = new PassStation();

    private StationSubsetter staEffectiveSubsetter = new PassStation();

    private List<ChannelSubsetter> chanSubsetters = new ArrayList<ChannelSubsetter>();

    private ChannelSubsetter chanEffectiveSubsetter = new PassChannel();

    private ChannelGrouper channelGrouper = new ChannelGrouper();
    

    protected NetworkFinder getNetworkSource() {
        return finder;
    }
    
    protected NetworkDB getNetworkDB() {
        return NetworkDB.getSingleton();
    }

    /** null means that we have not yet gotten nets from the server, so wait. */
    private List<CacheNetworkAccess> cacheNets;

    private HashSet<String> allStationFailureNets = new HashSet<String>();

    private HashSet<String> allChannelFailureStations = new HashSet<String>();
    
    private HashSet<String> allChannelGroupFailureStations = new HashSet<String>();

    private List<NetworkMonitor> statusMonitors = new ArrayList<NetworkMonitor>();

    private List<ArmListener> armListeners = new ArrayList<ArmListener>();

    private static Logger logger = Logger.getLogger(NetworkArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.NetworkArm");

    private boolean armFinished = false;
    
    RefreshNetworkArm refresh;
    
    private QueryTime lastQueryTime = null;
    
    final Object netGetSync = new Object();

    final Object staGetSync = new Object();

    final Object chanGetSync = new Object();
}// NetworkArm
