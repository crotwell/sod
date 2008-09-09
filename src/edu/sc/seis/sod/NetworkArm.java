package edu.sc.seis.sod;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.omg.CORBA.BAD_PARAM;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.VirtualNetworkHelper;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.DBCacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.source.event.EventSource;
import edu.sc.seis.sod.source.network.NetworkFinder;
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

    public NetworkArm(Element config) throws SQLException,
            ConfigurationException {
        processConfig(config);
    }

    public void run() {
        try {
            getSuccessfulNetworks();
        } catch(Throwable e) {
            Start.armFailure(this, e);
            armFinished = true;
        }
    }

    public boolean isActive() {
        return !armFinished;
    }

    public String getName() {
        return "NetworkArm";
    }

    public CacheNetworkAccess getNetwork(NetworkId network_id)
            throws NetworkNotFound {
        CacheNetworkAccess[] netDbs = getSuccessfulNetworks();
        MicroSecondDate beginTime = new MicroSecondDate(network_id.begin_time);
        String netCode = network_id.network_code;
        for(int i = 0; i < netDbs.length; i++) {
            NetworkAttr attr = netDbs[i].get_attributes();
            if(netCode.equals(attr.get_code())
                    && new MicroSecondTimeRange(attr.getEffectiveTime()).contains(beginTime)) {
                return netDbs[i];
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
            chanSubsetters.add(sodElement);
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
        return finder.getNetworkDC();
    }

    public TimeInterval getRefreshInterval() {
        return finder.getRefreshInterval();
    }

    /**
     * returns an array of SuccessfulNetworks. if the refreshInterval is valid
     * it gets the networks from the database(may be embedded or external). if
     * not it gets the networks again from the network server specified in the
     * networkFinder. After obtaining the Networks if processes them using the
     * NetworkSubsetter and returns the successful networks as an array of
     * NetworkDbObjects.
     * 
     * @throws NetworkNotFound
     */
    public CacheNetworkAccess[] getSuccessfulNetworks() throws NetworkNotFound {
        synchronized(netGetSync) {
            SodDB sodDb = SodDB.getSingleton();
            if(lastQueryTime == null) {
                // try from db
                lastQueryTime = sodDb.getQueryTime(finder.getName(),
                                                   finder.getDNS());
            }
            if(lastQueryTime == null) {
                // still null, must be first time
                lastQueryTime = new QueryTime(finder.getName(),
                                              finder.getDNS(),
                                              ClockUtil.now().getTimestamp());
                sodDb.putQueryTime(lastQueryTime);
            } else if(!lastQueryTime.needsRefresh(finder.getRefreshInterval())) {
                if(cacheNets == null) {
                    cacheNets = getNetworkDB().getAllNets(getNetworkDC());
                    for(int i = 0; i < cacheNets.length; i++) {
                        // this is for the side effect of creating
                        // networkInfoTemplate stuff
                        // avoids a null ptr later
                        change(cacheNets[i],
                               Status.get(Stage.NETWORK_SUBSETTER,
                                          Standing.SUCCESS));
                    }
                }
                return cacheNets;
            }
            statusChanged("Getting networks");
            logger.info("Getting networks");
            ArrayList successes = new ArrayList();
            CacheNetworkAccess[] allNets;
            NetworkDCOperations netDC = finder.getNetworkDC();
            synchronized(netDC) {
                String[] constrainingCodes = getConstrainingNetworkCodes(attrSubsetter);
                if(constrainingCodes.length > 0) {
                    edu.iris.Fissures.IfNetwork.NetworkFinder netFinder = netDC.a_finder();
                    List<CacheNetworkAccess> constrainedNets = new ArrayList<CacheNetworkAccess>(constrainingCodes.length);
                    for(int i = 0; i < constrainingCodes.length; i++) {
                        CacheNetworkAccess[] found;
                        // this is a bit of a hack as names could be one or two
                        // characters, but works with _US-TA style
                        // virtual networks at the DMC
                        if(constrainingCodes[i].length() > 2) {
                            found = (CacheNetworkAccess[])netFinder.retrieve_by_name(constrainingCodes[i]);
                        } else {
                            found = (CacheNetworkAccess[])netFinder.retrieve_by_code(constrainingCodes[i]);
                        }
                        for(int j = 0; j < found.length; j++) {
                            constrainedNets.add(found[j]);
                        }
                    }
                    allNets = (CacheNetworkAccess[])constrainedNets.toArray(new CacheNetworkAccess[0]);
                } else {
                    NetworkAccess[] tmpNets = netDC.a_finder().retrieve_all();
                    ArrayList goodNets = new ArrayList();
                    for(int i = 0; i < tmpNets.length; i++) {
                        try {
                            VirtualNetworkHelper.narrow(tmpNets[i]);
                            // Ignore any virtual nets returned here
                            logger.debug("ignoring virtual network "
                                    + tmpNets[i].get_attributes().get_code());
                            continue;
                        } catch(BAD_PARAM bp) {
                            // Must be a concrete, keep it
                            goodNets.add(tmpNets[i]);
                        }
                    }
                    allNets = (CacheNetworkAccess[])goodNets.toArray(new CacheNetworkAccess[0]);
                }
            }
            logger.info("Found " + allNets.length + " networks");
            NetworkPusher lastPusher = null;
            for(int i = 0; i < allNets.length; i++) {
                allNets[i] = new DBCacheNetworkAccess(allNets[i]);
                NetworkAttrImpl attr = null;
                try {
                    attr = (NetworkAttrImpl)allNets[i].get_attributes();
                    if(netEffectiveSubsetter.accept(attr).isSuccess()) {
                        if(attrSubsetter.accept(attr).isSuccess()) {
                            NetworkDB ndb = getNetworkDB();
                            int dbid = ndb.put(attr);
                            NetworkDB.commit();
                            logger.info("store network: " + attr.getDbid()
                                    + " " + dbid);
                            successes.add(allNets[i]);
                            change(allNets[i],
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.SUCCESS));
                            if(Start.getWaveformArm() == null
                                    && (! stationSubsetter.getClass().equals(PassStation.class) 
                                    || chanSubsetters.size() != 0 )) {
                                // only do netpushers if there are more subsetters downstream
                                // and the waveform arm does not exist, otherwise there is no point
                                // or we can let the waveform processors handle via EventNetworkPair
                                if (lastPusher == null) {
                                    lastPusher = new NetworkPusher(allNets[i].get_attributes());
                                    netPopulators.invokeLater(lastPusher);
                                } else {
                                    lastPusher.addNetwork(allNets[i].get_attributes());
                                }
                                logger.debug("running network");
                                //lastPusher.runNetwork(allNets[i].get_attributes());
                            }
                        } else {
                            change(allNets[i],
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.REJECT));
                            failLogger.info(NetworkIdUtil.toString(allNets[i].get_attributes()
                                    .get_id())
                                    + " was rejected.");
                        }
                    } else {
                        change(allNets[i], Status.get(Stage.NETWORK_SUBSETTER,
                                                      Standing.REJECT));
                        failLogger.info(NetworkIdUtil.toString(allNets[i].get_attributes()
                                .get_id())
                                + " was rejected because it wasn't active during the time range of requested events");
                    }
                } catch(Throwable th) {
                    GlobalExceptionHandler.handle("Got an exception while trying getSuccessfulNetworks for the "
                                                          + i
                                                          + "th networkAccess ("
                                                          +(attr==null?"null":NetworkIdUtil.toStringNoDates(((NetworkAttrImpl)allNets[i].get_attributes()))),
                                                  th);
                }
            }
            if(lastPusher != null) {
                lastPusher.setLastPusher();
            } else {
                // no pushers
                if(allNets.length == 0) {
                    logger.warn("Found no networks.  Make sure the network codes you entered are valid");
                }
                finish();
            }
            // Set the time of the last check to now
            lastQueryTime.setTime(ClockUtil.now().getTimestamp());
            cacheNets = new CacheNetworkAccess[successes.size()];
            successes.toArray(cacheNets);
            logger.info(cacheNets.length + " networks passed");
            statusChanged("Waiting for a request");
            return cacheNets;
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

    /**
     * retrieves all the stations, sites and channels for a network to populate
     * the db and cache
     */
    private class NetworkPusher implements Runnable {

        public NetworkPusher(NetworkAttrImpl net) {
            netList.add(net);
        }

        public void run() {
            logger.debug("Start NetworkPusher");
            NetworkAttrImpl n = netList.poll();
            while ( ! lastPusher || n != null) {
                if (n != null) {
                    runNetwork(n);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch(InterruptedException e) {}
                }
                n = netList.poll();
            }
            logger.debug("Finish NetworkPusher");
            synchronized(this) {
                pusherFinished = true;
                finishArm();
            }
        }
        
        void addNetwork(NetworkAttrImpl net) {
            logger.debug("Add to NetworkPusher "+net.get_code());
            netList.offer(net);
        }
        
        protected void runNetwork(NetworkAttrImpl net) {
            if(!Start.isArmFailure()) {
                logger.info("NetworkPusher Starting work on " + net.get_code());
                // new thread, so need to attach net attr instance
                NetworkDB ndb = getNetworkDB();
                try {
                    NetworkDB.getSession().lock(net,
                                                LockMode.NONE);
                } catch(NonUniqueObjectException e) {
                    logger.error("Try to lock net="
                                         + NetworkIdUtil.toString(net)
                                         + " dbid="
                                         + ((NetworkAttrImpl)net).getDbid(),
                                 e);
                    throw e;
                }
                StationImpl[] staDbs = getSuccessfulStations(net);
                if(!(Start.getWaveformArm() == null && chanSubsetters.size() == 0)) {
                    // only get channels if there are subsetters or a
                    // waveform arm
                    for(int j = 0; j < staDbs.length; j++) {
                        // commit for each station
                        synchronized(NetworkArm.this) {
                            NetworkDB.getSession().lock(net,
                                                        LockMode.NONE);
                            List<ChannelImpl> chans = getSuccessfulChannels(staDbs[j]);
                        }
                    }
                } else {
                    logger.info("Not getting channels as no subsetters or waveform arm");
                }
                // make sure everything is cleaned up in hibernate session
                NetworkDB.commit();
            }
        }

        private void finishArm() {
            if(lastPusher && pusherFinished) {
                finish();
            }
        }

        public synchronized void setLastPusher() {
            lastPusher = true;
            finishArm();
        }

        private boolean lastPusher = false, pusherFinished = false;
        
        private Queue<NetworkAttrImpl> netList = new LinkedList<NetworkAttrImpl>();
    }

    private void finish() {
        armFinished = true;
        logger.info("Network arm finished.");
        for(Iterator iter = armListeners.iterator(); iter.hasNext();) {
            ArmListener listener = (ArmListener)iter.next();
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
        logger.debug("getSuccessfulStations");
        if(allStationFailureNets.contains(NetworkIdUtil.toStringNoDates(net))) {
            return new StationImpl[0];
        }
        synchronized(this) {
            // try db
            List<StationImpl> sta = getNetworkDB().getStationForNet((NetworkAttrImpl)net);
            if(sta.size() != 0) {
                logger.debug("getSuccessfulStations " + netCode + " - from db "
                        + sta.size());
                return sta.toArray(new StationImpl[0]);
            }
            // really no dice, go to server
            statusChanged("Getting stations for "
                    + net.getName());
            ArrayList<Station> arrayList = new ArrayList<Station>();
            try {
                CacheNetworkAccess netAccess = CacheNetworkAccess.create((NetworkAttrImpl)net,
                                                                         CommonAccess.getNameService());
                Station[] stations = netAccess.retrieve_stations();
                for(int i = 0; i < stations.length; i++) {
                    logger.debug("Station in NetworkArm: "
                            + StationIdUtil.toString(stations[i]));
                }
                for(int i = 0; i < stations.length; i++) {
                    // hibernate gets angry if the network isn't the same object
                    // as the one already in the thread's session
                    stations[i].setNetworkAttr(net);
                    StringTree effResult = staEffectiveSubsetter.accept(stations[i],
                                                                        netAccess);
                    if(effResult.isSuccess()) {
                        StringTree staResult = stationSubsetter.accept(stations[i],
                                                                       netAccess);
                        if(staResult.isSuccess()) {
                            int dbid = getNetworkDB().put((StationImpl)stations[i]);
                            logger.info("Store " + stations[i].get_code()
                                    + " as " + dbid + " in " + getNetworkDB());
                            arrayList.add(stations[i]);
                            change(stations[i],
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.SUCCESS));
                        } else {
                            change(stations[i],
                                   Status.get(Stage.NETWORK_SUBSETTER,
                                              Standing.REJECT));
                            failLogger.info(StationIdUtil.toString(stations[i].get_id())
                                    + " was rejected: " + staResult);
                        }
                    } else {
                        change(stations[i], Status.get(Stage.NETWORK_SUBSETTER,
                                                       Standing.REJECT));
                        failLogger.info(StationIdUtil.toString(stations[i].get_id())
                                + " was rejected based on its effective time not matching the range of requested events: "
                                + effResult);
                    }
                }
                NetworkDB.commit();
            } catch(Exception e) {
                GlobalExceptionHandler.handle("Problem in method getSuccessfulStations for net "
                                                      + NetworkIdUtil.toString(net.get_id()),
                                              e);
            }
            StationImpl[] rtnValues = new StationImpl[arrayList.size()];
            rtnValues = (StationImpl[])arrayList.toArray(rtnValues);
            statusChanged("Waiting for a request");
            logger.debug("getSuccessfulStations " + netCode + " - from server "
                    + rtnValues.length);
            if(rtnValues.length == 0) {
                allStationFailureNets.add(NetworkIdUtil.toStringNoDates(net));
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
        if(allChannelFailureStations.contains(StationIdUtil.toStringNoDates(station))) {
            return new ArrayList<ChannelImpl>(0);
        }
        synchronized(this) {
            // no dice, try db
            List<ChannelImpl> sta = getNetworkDB().getChannelsForStation(station);
            if(sta.size() != 0) {
                logger.debug("successfulChannels " + station.get_code()
                        + " - from db " + sta.size());
                return sta;
            }
            // really no dice, go to server
            statusChanged("Getting channels for " + station);
            List<ChannelImpl> successes = new ArrayList<ChannelImpl>();
            try {
                CacheNetworkAccess networkAccess = CacheNetworkAccess.create((NetworkAttrImpl)station.getNetworkAttr(),
                                                                             CommonAccess.getNameService());
                Channel[] tmpChannels = networkAccess.retrieve_for_station(station.get_id());
                MicroSecondDate stationBegin = new MicroSecondDate(station.getBeginTime());
                // dmc network server ignores date in station id in
                // retrieve_for_station, so all channels for station code are
                // returned. This checks to make sure the station is the same.
                // ProxyNetworkAccess already interns stations in channels
                // so as long as station begin times are the same, they are
                // equal...we hope
                ArrayList<ChannelImpl> chansAtStation = new ArrayList<ChannelImpl>();
                for(int i = 0; i < tmpChannels.length; i++) {
                    if(new MicroSecondDate(tmpChannels[i].getSite().getStation().getBeginTime()).equals(stationBegin)) {
                        chansAtStation.add((ChannelImpl)tmpChannels[i]);
                    } else {
                        logger.info("Channel "
                                + ChannelIdUtil.toString(tmpChannels[i].get_id())
                                + " has a station that is not the same as the requested station: req="
                                + StationIdUtil.toString(station.get_id())
                                + "  chan sta="
                                + StationIdUtil.toString(tmpChannels[i].getSite()
                                        .getStation())+"  "+tmpChannels[i].getSite().getStation().getBeginTime().date_time+" != "+station.getBeginTime().date_time);
                    }
                }
                ChannelImpl[] channels = (ChannelImpl[])chansAtStation.toArray(new ChannelImpl[0]);
                Status inProg = Status.get(Stage.NETWORK_SUBSETTER,
                                           Standing.IN_PROG);
                boolean needCommit = false;
                for(int i = 0; i < channels.length; i++) {
                    ChannelImpl chan = channels[i];
                    change(chan, inProg);
                    StringTree effectiveTimeResult = chanEffectiveSubsetter.accept(chan,
                                                                                   networkAccess);
                    if(effectiveTimeResult.isSuccess()) {
                        boolean accepted = true;
                        synchronized(chanSubsetters) {
                            Iterator it = chanSubsetters.iterator();
                            while(it.hasNext()) {
                                ChannelSubsetter cur = (ChannelSubsetter)it.next();
                                StringTree result = cur.accept(chan,
                                                               networkAccess);
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
                            Integer dbidInt = new Integer(getNetworkDB().put(chan));
                            needCommit = true;
                            channelMap.put(dbidInt, chan);
                            channelToSiteMap.put(dbidInt, station);
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
            }
            return successes;
        }
    }

    public List<ChannelGroup> getSuccessfulChannelGroups(StationImpl station) {
        if(allChannelFailureStations.contains(StationIdUtil.toStringNoDates(station))) {
            return new ArrayList<ChannelGroup>(0);
        }
        synchronized(this) {
            // no dice, try db
            List<ChannelGroup> sta = getNetworkDB().getChannelGroupsForStation(station);
            if(sta.size() != 0) {
                logger.debug("successfulChannelGroups " + station.get_code()
                        + " - from db " + sta.size());
                return sta;
            }
            // really no dice, go to server
            List<ChannelImpl> failures = new ArrayList<ChannelImpl>();
            List<ChannelGroup> chanGroups = channelGrouper.group(getSuccessfulChannels(station),
                                                             failures);
            for(ChannelGroup cg : chanGroups) {
                getNetworkDB().put(cg);
            }
            if (chanGroups.size() != 0) {
                getNetworkDB().commit();
            }
            Iterator it = failures.iterator();
            Status chanReject = Status.get(Stage.NETWORK_SUBSETTER,
                                           Standing.REJECT);
            while(it.hasNext()) {
                Channel failchan = (Channel)it.next();
                failLogger.info(ChannelIdUtil.toString(failchan.get_id())
                        + "  Channel not grouped.");
            }
            return chanGroups;
        }
    }

    // This is a HACK. Since we're already storing the channels whole hog, it
    // isn't much of a stretch to cache them by dbid, and this allows
    // JDBCEventChannelStatus to quickly pull them out instead of going to the
    // Net database
    public Channel getChannel(int chanId) throws NotFound, SQLException {
        ChannelImpl chan = (ChannelImpl)channelMap.get(new Integer(chanId));
        if(chan != null) {
            return chan;
        }
        chan = getNetworkDB().getChannel(chanId);
        channelMap.put(new Integer(chanId), chan);
        return chan;
    }

    private QueryTime lastQueryTime = null;

    private Map channelMap = Collections.synchronizedMap(new HashMap());

    private Map channelToSiteMap = Collections.synchronizedMap(new HashMap());

    private void statusChanged(String newStatus) {
        synchronized(statusMonitors) {
            Iterator it = statusMonitors.iterator();
            while(it.hasNext()) {
                try {
                    ((NetworkMonitor)it.next()).setArmStatus(newStatus);
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
            Iterator it = statusMonitors.iterator();
            while(it.hasNext()) {
                try {
                    ((NetworkMonitor)it.next()).change(chan, newStatus);
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
            Iterator it = statusMonitors.iterator();
            while(it.hasNext()) {
                try {
                    ((NetworkMonitor)it.next()).change(sta, newStatus);
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
            Iterator it = statusMonitors.iterator();
            while(it.hasNext()) {
                try {
                    ((NetworkMonitor)it.next()).change(na, newStatus);
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

    // Since we synchronize around the NetDC, only 1 thread can get stuff from
    // the network server at the same time. The pool is here in the off chance
    // the IRIS Network Server is fixed and we can run multiple threads to fill
    // up the db. If so, remove SynchronizedNetworkAccess from
    // BulletproofVestFactory, increase the number of threads here, and chuckle
    // as the stations stream in.
    private WorkerThreadPool netPopulators = new WorkerThreadPool("NetPopulator",
                                                                  1);

    private NetworkFinder finder = null;

    private NetworkSubsetter attrSubsetter = new PassNetwork();

    private NetworkSubsetter netEffectiveSubsetter = new PassNetwork();

    private StationSubsetter stationSubsetter = new PassStation();

    private StationSubsetter staEffectiveSubsetter = new PassStation();

    private List chanSubsetters = new ArrayList();

    private ChannelSubsetter chanEffectiveSubsetter = new PassChannel();

    private ChannelGrouper channelGrouper = new ChannelGrouper();

    protected NetworkDB getNetworkDB() {
        return NetworkDB.getSingleton();
    }

    private CacheNetworkAccess[] cacheNets;

    private HashSet<String> allStationFailureNets = new HashSet<String>();

    private HashSet<String> allChannelFailureStations = new HashSet<String>();

    private List statusMonitors = new ArrayList();

    private List armListeners = new ArrayList();

    private static Logger logger = Logger.getLogger(NetworkArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.NetworkArm");

    private boolean armFinished = false;

    private final Object netGetSync = new Object();

    private final Object staGetSync = new Object();

    private final Object chanGetSync = new Object();
}// NetworkArm
