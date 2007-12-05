package edu.sc.seis.sod;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
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
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAccess;
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
import edu.sc.seis.sod.subsetter.site.SiteSubsetter;
import edu.sc.seis.sod.subsetter.station.PassStation;
import edu.sc.seis.sod.subsetter.station.StationEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

public class NetworkArm implements Arm {

    public NetworkArm(Element config) throws SQLException,
            ConfigurationException {
        networkDB = new NetworkDB();
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

    public CacheNetworkAccess getNetwork(NetworkId network_id) throws NetworkNotFound {
        CacheNetworkAccess[] netDbs = getSuccessfulNetworks();
        MicroSecondDate beginTime = new MicroSecondDate(network_id.begin_time);
        String netCode = network_id.network_code;
        for(int i = 0; i < netDbs.length; i++) {
            NetworkAttr attr = netDbs[i].get_attributes();
            if(netCode.equals(attr.get_code())
                    && new MicroSecondTimeRange(attr.effective_time).contains(beginTime)) {
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

    private void loadConfigElement(Object sodElement) throws ConfigurationException {
        if(sodElement instanceof NetworkFinder) {
            finder = (NetworkFinder)sodElement;
        } else if(sodElement instanceof NetworkSubsetter) {
            attrSubsetter = (NetworkSubsetter)sodElement;
        } else if(sodElement instanceof StationSubsetter) {
            stationSubsetter = (StationSubsetter)sodElement;
        } else if(sodElement instanceof SiteSubsetter) {
            final SiteSubsetter siteSubsetter = (SiteSubsetter)sodElement;
            chanSubsetters.add(new ChannelSubsetter() {
                public StringTree accept(Channel channel,
                                         ProxyNetworkAccess network)
                        throws Exception {
                    return siteSubsetter.accept(channel.my_site, network);
                }
            });
        } else if(sodElement instanceof ChannelSubsetter) {
            chanSubsetters.add(sodElement);
        } else {
            throw new ConfigurationException("Unknown configuration object: "+sodElement);
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
     * @throws NetworkNotFound 
     */
    public synchronized CacheNetworkAccess[] getSuccessfulNetworks() throws NetworkNotFound {
        SodDB sodDb = new SodDB();
        logger.info("SodDB in NetworkArm:"+sodDb);
        QueryTime qtime = sodDb.getQueryTime(finder.getName(),
                                             finder.getDNS());
        if (qtime == null) {
            qtime = new QueryTime(finder.getName(),
                                  finder.getDNS(),
                                  ClockUtil.now().getTimestamp());
            sodDb.putQueryTime(qtime);
        } else if(!qtime.needsRefresh(finder.getRefreshInterval())) {
            if(netDbs == null) {
                netDbs = getNetworkDB().getAllNets(getNetworkDC());
                for(int i = 0; i < netDbs.length; i++) {
                    // this is for the side effect of creating
                    // networkInfoTemplate stuff
                    // avoids a null ptr later
                    change(netDbs[i],
                           Status.get(Stage.NETWORK_SUBSETTER, Standing.SUCCESS));
                }
            }
            sodDb.commit();
            return netDbs;
        }
        statusChanged("Getting networks");
        logger.info("Getting networks");
        ArrayList networkDBs = new ArrayList();
        CacheNetworkAccess[] allNets;
        
        NetworkDCOperations netDC = finder.getNetworkDC();
        synchronized(netDC) {
            String[] constrainingCodes = getConstrainingNetworkCodes(attrSubsetter);
            if(constrainingCodes.length > 0) {
                edu.iris.Fissures.IfNetwork.NetworkFinder netFinder = netDC.a_finder();
                List constrainedNets = new ArrayList(constrainingCodes.length);
                for(int i = 0; i < constrainingCodes.length; i++) {
                    NetworkAccess[] found;
                    // this is a bit of a hack as names could be one or two
                    // characters, but works with _US-TA style
                    // virtual networks at the DMC
                    if(constrainingCodes[i].length() > 2) {
                        found = netFinder.retrieve_by_name(constrainingCodes[i]);
                    } else {
                        found = netFinder.retrieve_by_code(constrainingCodes[i]);
                    }
                    for(int j = 0; j < found.length; j++) {
                        constrainedNets.add(found[j]);
                    }
                }
                allNets = (CacheNetworkAccess[])constrainedNets.toArray(new CacheNetworkAccess[0]);
            } else {
                NetworkAccess[] tmpNets = netDC.a_finder().retrieve_all();
                allNets = new CacheNetworkAccess[tmpNets.length];
                System.arraycopy(tmpNets, 0, allNets, 0, tmpNets.length);
            }
        }
        logger.info("Found " + allNets.length + " networks");
        NetworkPusher lastPusher = null;
        for(int i = 0; i < allNets.length; i++) {
            try {
                VirtualNetworkHelper.narrow(allNets[i]);
                // Ignore any virtual nets returned here
                logger.debug("ignoring virtual network "
                        + allNets[i].get_attributes().get_code());
                continue;
            } catch(BAD_PARAM bp) {
                // Must be a concrete, continue
            }
            try {
                NetworkAttrImpl attr = (NetworkAttrImpl)allNets[i].get_attributes();
                if(netEffectiveSubsetter.accept(attr).isSuccess()) {
                    if(attrSubsetter.accept(attr).isSuccess()) {
                        NetworkDB ndb = getNetworkDB();
                        int dbid= ndb.put(attr);
                        logger.debug("Session: "+getNetworkDB().getSession().getStatistics().toString());
                        ndb.commit();
                        logger.info("store network: "+attr.getDbid()+" "+dbid);
                        networkDBs.add(allNets[i]);
                        change(allNets[i], Status.get(Stage.NETWORK_SUBSETTER,
                                                      Standing.SUCCESS));
                        if(!(Start.getWaveformArm() == null
                                && stationSubsetter.getClass()
                                        .equals(PassStation.class)
                                && chanSubsetters.size() == 0)) {
                            // only do netpushers if there are more subsetters
                            // downstream
                            // or the waveform arm exists, otherwise there is no
                            // point
                            lastPusher = new NetworkPusher(allNets[i]);
                            netPopulators.invokeLater(lastPusher);
                        }
                    } else {
                        change(allNets[i], Status.get(Stage.NETWORK_SUBSETTER,
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
                                                      + i + "th networkAccess",
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
        qtime.setTime(ClockUtil.now().getTimestamp());
        sodDb.commit();
        netDbs = new CacheNetworkAccess[networkDBs.size()];
        networkDBs.toArray(netDbs);
        logger.info(netDbs.length + " networks passed");
        statusChanged("Waiting for a request");
        return netDbs;
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

        public NetworkPusher(CacheNetworkAccess net) {
            this.net = net;
        }

        public void run() {
            if(!Start.isArmFailure()) {
                logger.info("Starting work on " + net);
                // new thread, so need to attach net attr instance
                NetworkDB ndb = getNetworkDB();
                StationImpl[] staDbs = getSuccessfulStations(net);
                ndb.flush();
                if(!(Start.getWaveformArm() == null
                        &&  chanSubsetters.size() == 0)) {
                    // only get channels if there are subsetters or a
                    // waveform arm
                    for(int j = 0; j < staDbs.length; j++) {
                    	// commit for each station
                    	synchronized (NetworkArm.this) {
                        ndb.getSession().lock(net.get_attributes(), LockMode.NONE);
                        ChannelImpl[] chans = getSuccessfulChannels(net,
                                                                    staDbs[j]);
						}
                    }
                } else {
                    logger.info("Not getting channels as no subsetters or waveform arm");
                }
            }
            synchronized(this) {
                pusherFinished = true;
                finishArm();
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

        private CacheNetworkAccess net;
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
        String netCode =  net.get_attributes().get_code();
        logger.debug("getSuccessfulStations");
        Integer netKey = new Integer(((NetworkAttrImpl)net.get_attributes()).getDbid());
        if(successfulStations.get(netKey) != null) {
            logger.debug("getSuccessfulStations "+netCode+" - from cache "+((StationImpl[])successfulStations.get(netKey)).length);
            return (StationImpl[])successfulStations.get(netKey);
        }
        synchronized(this) {
            // recheck in sync just in case something changed in the last nanosecond
            if(successfulStations.get(netKey) != null) {
                logger.debug("getSuccessfulStations "+netCode+" - from cache2");
                return (StationImpl[])successfulStations.get(netKey);
            }
            // no dice, try db
            StationImpl[] sta = getNetworkDB().getStationForNet((NetworkAttrImpl)net.get_attributes());
            if (sta.length != 0) {
                successfulStations.put(netKey, sta);
                logger.debug("getSuccessfulStations "+netCode+" - from db "+sta.length);
                return sta;
            }
            // really no dice, go to server
            statusChanged("Getting stations for "
                    + net.get_attributes().name);
            ArrayList arrayList = new ArrayList();
            try {
                Station[] stations = net.retrieve_stations();
                for(int i = 0; i < stations.length; i++) {
                    StringTree effResult = staEffectiveSubsetter.accept(stations[i],
                                                                        net);
                    if(effResult.isSuccess()) {
                        StringTree staResult = stationSubsetter.accept(stations[i],
                                                                       net);
                        if(staResult.isSuccess()) {
                            int dbid = getNetworkDB().put((StationImpl)stations[i]);
                            logger.info("Store "+stations[i].get_code()+" as "+dbid+" in "+getNetworkDB());
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
                        change(stations[i],
                               Status.get(Stage.NETWORK_SUBSETTER,
                                          Standing.REJECT));
                        failLogger.info(StationIdUtil.toString(stations[i].get_id())
                                + " was rejected based on its effective time not matching the range of requested events: "
                                + effResult);
                    }
                }
            } catch(Exception e) {
                GlobalExceptionHandler.handle("Problem in method getSuccessfulStations for net "
                                                      + NetworkIdUtil.toString(net.get_attributes()
                                                              .get_id()),
                                              e);
            }
            StationImpl[] rtnValues = new StationImpl[arrayList.size()];
            rtnValues = (StationImpl[])arrayList.toArray(rtnValues);
            successfulStations.put(netKey, rtnValues);
            statusChanged("Waiting for a request");
            logger.debug("getSuccessfulStations "+netCode+" - from server "+rtnValues.length);
            return rtnValues;
        }
    }

    /**
     * Obtains the Channels corresponding to the siteDbObject, processes them
     * using the ChannelSubsetter and returns an array of those that pass
     * @throws NetworkNotFound 
     */
    public ChannelImpl[] getSuccessfulChannels(StationImpl siteDbObject) throws NetworkNotFound {
        Integer staKey = new Integer(siteDbObject.getDbid());
        if (successfulChannels.containsKey(staKey)) {
            return (ChannelImpl[])successfulChannels.get(staKey);
        }
        return getSuccessfulChannels(getNetwork(siteDbObject.my_network.get_id()), siteDbObject);
    }
    
    public ChannelImpl[] getSuccessfulChannels(CacheNetworkAccess networkAccess, StationImpl station) {
        Integer staKey = new Integer(station.getDbid());
        if (successfulChannels.containsKey(staKey)) {
            return (ChannelImpl[])successfulChannels.get(staKey);
        }
        synchronized(this) {
            // check again inside sync in case something change in the last nanosecond
            if (successfulChannels.containsKey(staKey)) {
                return (ChannelImpl[])successfulChannels.get(staKey);
            }
            statusChanged("Getting channels for " + station);
            List successes = new ArrayList();
            try {
                Channel[] tmpChannels = networkAccess.retrieve_for_station(station.get_id());
                ChannelImpl[] channels = new ChannelImpl[tmpChannels.length];
                System.arraycopy(tmpChannels, 0, channels, 0, channels.length);
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
                if (needCommit) {
                    getNetworkDB().commit();
                }
            } catch(Throwable e) {
                GlobalExceptionHandler.handle("Problem in method getSuccessfulChannels for "
                                                      + StationIdUtil.toString(station.get_id()),
                                              e);
                getNetworkDB().rollback();
            }
            ChannelImpl[] values = (ChannelImpl[])successes.toArray(new ChannelImpl[0]);
            successfulChannels.put(staKey, values);
            statusChanged("Waiting for a request");
            return values;
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
    
    private Map successfulStations = new HashMap();

    private List chanSubsetters = new ArrayList();

    private ChannelSubsetter chanEffectiveSubsetter = new PassChannel();
    
    private Map successfulChannels = new HashMap();
    
    protected NetworkDB getNetworkDB() {
        return networkDB;
    }
    
    private NetworkDB networkDB;

    private CacheNetworkAccess[] netDbs;

    private List statusMonitors = new ArrayList();

    private List armListeners = new ArrayList();

    private static Logger logger = Logger.getLogger(NetworkArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.NetworkArm");

    private boolean armFinished = false;
}// NetworkArm
