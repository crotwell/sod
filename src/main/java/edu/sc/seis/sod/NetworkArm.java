package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.event.EventSource;
import edu.sc.seis.sod.source.network.FdsnStation;
import edu.sc.seis.sod.source.network.InstrumentationFromDB;
import edu.sc.seis.sod.source.network.LoadedNetworkSource;
import edu.sc.seis.sod.source.network.NetworkQueryConstraints;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.source.network.RetryNetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.networkArm.NetworkMonitor;
import edu.sc.seis.sod.subsetter.channel.ChannelEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.channel.PassChannel;
import edu.sc.seis.sod.subsetter.network.NetworkEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;
import edu.sc.seis.sod.subsetter.network.PassNetwork;
import edu.sc.seis.sod.subsetter.station.PassStation;
import edu.sc.seis.sod.subsetter.station.StationEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

public class NetworkArm implements Arm {

    public NetworkArm(Element config) throws ConfigurationException {
        channelGrouper = new ChannelGrouper(Start.getRunProps().getChannelGroupingRules());
        processConfig(config);
        refresh = new RefreshNetworkArm(this);
    }

    public void run() {
        try {
            SodDB sodDb = SodDB.getSingleton();
            // lastQueryTime should be null if first time
            lastQueryTime = sodDb.getQueryTime(getInternalNetworkSource().getName(), "");
            
            // only do timer if positive interval and waveform arm exists, otherwise run in thread
            if (getRefreshInterval().value > 0 && Start.getWaveformRecipe() != null) {
                Timer timer = new Timer("Refresh NetworkArm", true);
                long period = (long)getInternalNetworkSource().getRefreshInterval().getValue(UnitImpl.MILLISECOND);
                long firstDelay = lastQueryTime==null ? 0 : lastQueryTime.delayUntilNextRefresh(getRefreshInterval());
                logger.debug("Refresh timer startup: period: "+period+"  firstDelay: "+firstDelay+"  last query: "+(lastQueryTime==null ? "null" : lastQueryTime.getTime()));
                timer.schedule(refresh, firstDelay, period);
                if (period == 0) {
                    try { Thread.sleep(10);  } catch(InterruptedException e) { } // give refresh time to start up
                }
                initialStartupFinished = true;
            } else {
                // no refresh, so do net arm in this thread once
                initialStartupFinished = true;
                refresh.run();
            }
            // make sure any open read only db connetion is closed (ie from lastQueryTime above)
            SodDB.rollback();
        } catch(Throwable e) {
            armFinished = true;
            Start.armFailure(this, e);
        }
    }

    public boolean isActive() {
        return !armFinished;
    }

    public String getName() {
        return "NetworkArm";
    }

    public NetworkAttrImpl getNetwork(NetworkId network_id)
            throws NetworkNotFound {
        List<NetworkAttrImpl> netDbs = getSuccessfulNetworks();
        MicroSecondDate beginTime = new MicroSecondDate(network_id.begin_time);
        String netCode = network_id.network_code;
        for (NetworkAttrImpl attr : netDbs) {
            if(netCode.equals(attr.get_code())
                    && new MicroSecondTimeRange(attr.getEffectiveTime()).contains(beginTime)) {
                return attr;
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
        
        edu.iris.Fissures.TimeRange timeRange = configureEffectiveTimeCheckers();
        NetworkQueryConstraints constraints = new NetworkQueryConstraints(attrSubsetter,
                                                                          stationSubsetter,
                                                                          chanSubsetters,
                                                                          timeRange);
        getNetworkSource().setConstraints(constraints);
    }

    private edu.iris.Fissures.TimeRange configureEffectiveTimeCheckers() {
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
            return eventQueryTimes;
        } else {
            logger.debug("No implicit effective time constraint");
            return null;
        }
    }

    public static final String[] PACKAGES = {"networkArm",
                                             "channel",
                                             "site",
                                             "station",
                                             "network"};

    private void loadConfigElement(Object sodElement)
            throws ConfigurationException {
        if(sodElement instanceof NetworkSource) {
            internalFinder = new RetryNetworkSource((NetworkSource)sodElement);
            finder = new InstrumentationFromDB(internalFinder);
        } else if(sodElement instanceof NetworkSubsetter) {
            attrSubsetter = (NetworkSubsetter)sodElement;
        } else if(sodElement instanceof StationSubsetter) {
            stationSubsetter = (StationSubsetter)sodElement;
        } else if(sodElement instanceof ChannelSubsetter) {
            chanSubsetters.add((ChannelSubsetter)sodElement);
        } else {
            throw new ConfigurationException("Unknown configuration object: "
                    + sodElement.getClass());
        }
    }

    public void add(NetworkMonitor monitor) {
        synchronized(statusMonitors) {
            statusMonitors.add(monitor);
        }
    }

    public TimeInterval getRefreshInterval() {
        return getInternalNetworkSource().getRefreshInterval();
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
    public List<NetworkAttrImpl> getSuccessfulNetworks() {
        synchronized(refresh) {
            /** null means that we have not yet gotten nets from the server, so wait. */
            List<NetworkAttrImpl> cacheNets = loadNetworksFromDB();
            while (cacheNets == null && lastQueryTime == null && ! Start.isArmFailure()) {
                // still null, maybe first time through
                logger.info("Waiting on initial network load");
                refresh.notifyAll();
                try {
                    refresh.wait(1000);
                } catch(InterruptedException e) {
                }
                cacheNets = loadNetworksFromDB();
            }
            return cacheNets;
        }
    }
    
    List<NetworkAttrImpl> loadNetworksFromDB() {
        synchronized(netGetSync) { // don't get nets while being reloaded
        List<NetworkAttrImpl> fromDB = getNetworkDB().getAllNetworks();
        for (NetworkAttrImpl net : fromDB) {
            // this is for the side effect of creating
            // networkInfoTemplate stuff
            // avoids a null ptr later
            change(net,
                   Status.get(Stage.NETWORK_SUBSETTER,
                              Standing.SUCCESS));
        }
        return fromDB;
        }
    }

    List<NetworkAttrImpl> getSuccessfulNetworksFromServer() throws SodSourceException {
        synchronized(netGetSync) {
            statusChanged("Getting networks");
            logger.info("Getting networks from server");
            ArrayList<NetworkAttrImpl> successes = new ArrayList<NetworkAttrImpl>();
            List<? extends NetworkAttrImpl> allNets = getInternalNetworkSource().getNetworks();
            logger.info("Found " + allNets.size() + " networks");
            int i=0;
            for (NetworkAttrImpl attr : allNets) {
                try {
                    if(netEffectiveSubsetter.accept(attr).isSuccess()) {
                        StringTree result;
                        try {
                            result = attrSubsetter.accept(attr);
                        } catch(Throwable t) {
                            logger.debug("Network subsetter exception: ", t);
                            result = new Fail(attrSubsetter, "Exception", t);
                        }
                        if(result.isSuccess()) {
                            NetworkDB ndb = getNetworkDB();
                            int dbid = ndb.put(attr);
                            NetworkDB.commit();
                            logger.info("store network: " + NetworkIdUtil.toStringNoDates(attr)+" " + attr.getDbid()
                                    + " " + dbid);
                            successes.add(attr);
                            change(attr,
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.SUCCESS));
                        } else {
                            change(attr,
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.REJECT));
                            failLogger.info(NetworkIdUtil.toString(attr
                                    .get_id())
                                    + " was rejected. "+result);
                        }
                    } else {
                        change(attr, Status.get(Stage.NETWORK_SUBSETTER,
                                                      Standing.REJECT));
                        failLogger.info(NetworkIdUtil.toString(attr
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
                logger.warn(NO_NETWORKS_MSG);
                Start.armFailure(this, new NotFound(NO_NETWORKS_MSG));
            }
            logger.info(successes.size() + " networks passed");
            statusChanged("Waiting for a request");
            synchronized(refresh) {
                refresh.notifyAll();
            }
            return successes;
        }
    }


    void finish() {
        armFinished = true;
        lastQueryTime = new QueryTime(getInternalNetworkSource().getName(), "", ClockUtil.now().getTimestamp());
        SodDB.getSingleton().putQueryTime(lastQueryTime);
        SodDB.commit();
        logger.info("Network arm finished.");
        for (ArmListener listener : armListeners) {
            listener.finished(this);
        }
        if (Start.getEventArm() != null && Start.getEventArm().getWaveformArmSync() != null) {
            synchronized(Start.getEventArm().getWaveformArmSync()) {
                Start.getEventArm().getWaveformArmSync().notifyAll();
            }
        }
    }

    public void add(ArmListener listener) {
        armListeners.add(listener);
        if(armFinished == true) {
            listener.finished(this);
        }
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
    
    StationImpl[] getSuccessfulStationsFromServer(NetworkAttrImpl net) {
        synchronized(this) {
            NetworkAttrImpl netAttr;
            try {
                netAttr = NetworkDB.getSingleton().getNetwork(net.getDbid());
            } catch(NotFound e1) {
                // must not be in db yet???
                throw new RuntimeException("Network not in db yet: "+NetworkIdUtil.toString(net));
            }
            statusChanged("Getting stations for "
                    + net.getName());
            ArrayList<Station> arrayList = new ArrayList<Station>();
            try {
                List<? extends StationImpl> stations = getInternalNetworkSource().getStations(netAttr);
                /*
                 // network consistency for TA take a really long time due to pairwise comparison.
                if( ! NetworkConsistencyCheck.isConsistent(stations)) {
                    failLogger.warn("Inconsistent stations for network: "+NetworkIdUtil.toString(net));
                }
                */
                for (StationImpl stationImpl : stations) {
                    logger.debug("Station in NetworkArm: "
                            + StationIdUtil.toString(stationImpl));
                }
                for (StationImpl currStation : stations) {
                    /*
                     //disable for speed reasons
                    if (! NetworkConsistencyCheck.isConsistent(net, currStation)) {
                        failLogger.warn("Not consistent: "+StationIdUtil.toString(currStation.getId()));
                    }*/
                    // hibernate gets angry if the network isn't the same object
                    // as the one already in the thread's session
                    currStation.setNetworkAttr(netAttr);
                    StringTree effResult = staEffectiveSubsetter.accept(currStation,
                                                                        getNetworkSource());
                    if(effResult.isSuccess()) {
                        StringTree staResult;
                        try {
                            staResult = stationSubsetter.accept(currStation,
                                                                getNetworkSource());
                        } catch(Throwable t) {
                            logger.debug("Station subsetter exception: ", t);
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
                                + " was rejected because the station was not active during the time range of requested events: "
                                + effResult);
                    }
                }
                NetworkDB.commit();
            } catch(Exception e) {
                GlobalExceptionHandler.handle("Problem in method getSuccessfulStations for net "
                                                      + NetworkIdUtil.toString(net.get_id()),
                                              e);
                NetworkDB.rollback();
            }
            StationImpl[] rtnValues = new StationImpl[arrayList.size()];
            rtnValues = (StationImpl[])arrayList.toArray(rtnValues);
            statusChanged("Waiting for a request");
            logger.debug("getSuccessfulStations " + NetworkIdUtil.toStringNoDates(net) + " - from server "
                    + rtnValues.length);
            if(rtnValues.length == 0) {
                allStationFailureNets.add(NetworkIdUtil.toStringNoDates(net));
            } else {
                allStationFailureNets.remove(NetworkIdUtil.toStringNoDates(net));
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
            while(refresh.isNetworkBeingReloaded(((NetworkAttrImpl)station.getNetworkAttr()).getDbid())
                    && refresh.isStationBeingReloaded(station.getDbid())) {
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
    
    List<ChannelImpl> getSuccessfulChannelsFromServer(StationImpl station, LoadedNetworkSource loadedNetworkSource) {
        synchronized(this) {
            statusChanged("Getting channels for " + station);
            List<ChannelImpl> successes = new ArrayList<ChannelImpl>();
            try {               
                List<? extends ChannelImpl> chansAtStation = loadedNetworkSource.getChannels(station);
                Status inProg = Status.get(Stage.NETWORK_SUBSETTER,
                                           Standing.IN_PROG);
                boolean needCommit = false;
                StationImpl dbSta = NetworkDB.getSingleton().getStation(station.getDbid());
                for (ChannelImpl chan : chansAtStation) {
                    /*
                     // consistency check takes time and only does a warning, skip
                    if (! NetworkConsistencyCheck.isConsistent(station, chan)) {
                        failLogger.warn("Not consistent: "+ChannelIdUtil.toString(chan.getId()));
                    }
                    */
                    // make the assumption that the station in the channel is the same as the station retrieved earlier
                    chan.getSite().setStation(dbSta);
                    change(chan, inProg);
                    StringTree effectiveTimeResult = chanEffectiveSubsetter.accept(chan,
                                                                                   loadedNetworkSource);
                    NetworkDB.flush();
                    if(effectiveTimeResult.isSuccess()) {
                        NetworkDB.flush();
                        boolean accepted = true;
                        synchronized(chanSubsetters) {
                            for (ChannelSubsetter cur : chanSubsetters) {
                                StringTree result;
                                try {
                                    result = cur.accept(chan,
                                                        loadedNetworkSource);
                                } catch(Throwable t) {
                                    logger.debug("Channel subsetter exception: ", t);
                                    result = new Fail(cur, "Exception", t);
                                }
                                if(!result.isSuccess()) {
                                    change(chan,
                                           Status.get(Stage.NETWORK_SUBSETTER,
                                                      Standing.REJECT));
                                    String resultToString = result.toString();
                                    if (result.toString().trim().length() == 0) {
                                        resultToString = cur.getClass().getName();
                                    }
                                    failLogger.info("Rejected "
                                            + ChannelIdUtil.toString(chan.get_id())
                                            + ": " + resultToString);
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
                        failLogger.info(ChannelIdUtil.toString(chan.get_id())
                                + " was rejected because the channel was not active during the time range of requested events: "
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
            while(refresh.isNetworkBeingReloaded(((NetworkAttrImpl)station.getNetworkAttr()).getDbid())
                    && refresh.isStationBeingReloaded(station.getDbid())) {
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
    
    List<ChannelGroup> getSuccessfulChannelGroupsFromServer(StationImpl station, LoadedNetworkSource net) {
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
                        + "  Channel not grouped into 3 components.");
            }
            return chanGroups;
        }
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

    private void change(NetworkAttrImpl na, Status newStatus) {
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

    private NetworkSource internalFinder;

    private NetworkSource finder;
    
    private NetworkSubsetter attrSubsetter = new PassNetwork();

    private NetworkSubsetter netEffectiveSubsetter = new PassNetwork();

    private StationSubsetter stationSubsetter = new PassStation();

    private StationSubsetter staEffectiveSubsetter = new PassStation();

    private List<ChannelSubsetter> chanSubsetters = new ArrayList<ChannelSubsetter>();

    private ChannelSubsetter chanEffectiveSubsetter = new PassChannel();

    private ChannelGrouper channelGrouper;


    public NetworkSource getNetworkSource() {
        if (finder == null) {
            finder = new InstrumentationFromDB(getInternalNetworkSource());
        }
        return finder;
    }

     protected NetworkSource getInternalNetworkSource() {
         if (internalFinder == null) {
             internalFinder = new RetryNetworkSource(new FdsnStation()); 
         }
        return internalFinder;
    }
    
    protected NetworkDB getNetworkDB() {
        return NetworkDB.getSingleton();
    }
    
    public boolean isBeingRefreshed(NetworkAttrImpl net) {
        return refresh.isNetworkBeingReloaded(net.getDbid());
    }
    
    public boolean isBeingRefreshed(StationImpl sta) {
        return refresh.isStationBeingReloaded(sta.getDbid());
    }
    
    public RefreshNetworkArm getRefresher() {
        return refresh;
    }

    private HashSet<String> allStationFailureNets = new HashSet<String>();

    private HashSet<String> allChannelFailureStations = new HashSet<String>();
    
    private HashSet<String> allChannelGroupFailureStations = new HashSet<String>();

    private List<NetworkMonitor> statusMonitors = new ArrayList<NetworkMonitor>();

    private List<ArmListener> armListeners = new ArrayList<ArmListener>();

    private static Logger logger = LoggerFactory.getLogger(NetworkArm.class);

    private static final org.slf4j.Logger failLogger = org.slf4j.LoggerFactory.getLogger("Fail.NetworkArm");

    private boolean armFinished = false;
    
    private boolean initialStartupFinished = false;
    
    
    public boolean isInitialStartupFinished() {
        return initialStartupFinished;
    }

    RefreshNetworkArm refresh;
    
    private QueryTime lastQueryTime = null;
    
    final Object netGetSync = new Object();

    final Object staGetSync = new Object();

    final Object chanGetSync = new Object();

    public ChannelGrouper getChannelGrouper() {
        return channelGrouper;
    }
    
    public static final String NO_NETWORKS_MSG = "Found no networks.  Make sure the network codes you entered are valid. "
            +"This can also be caused by asking for a restricted networks without <includeRestricted>true</includeRestricted> in a <fdsnStation> network source.";
            
    
}// NetworkArm
