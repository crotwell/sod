package edu.sc.seis.sod;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.JDBCQueryTime;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.SiteDbObject;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.database.network.JDBCNetworkUnifier;
import edu.sc.seis.sod.database.network.JDBCNewChannels;
import edu.sc.seis.sod.process.networkArm.NetworkProcess;
import edu.sc.seis.sod.status.networkArm.NetworkMonitor;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.channel.PassChannel;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;
import edu.sc.seis.sod.subsetter.network.PassNetwork;
import edu.sc.seis.sod.subsetter.site.PassSite;
import edu.sc.seis.sod.subsetter.site.SiteSubsetter;
import edu.sc.seis.sod.subsetter.station.PassStation;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

/**
 * Handles the subsetting of the Channels. Created: Wed Mar 20 13:30:06 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
public class NetworkArm {

    public NetworkArm(Element config) throws ConfigurationException {
        try {
            queryTimeTable = new JDBCQueryTime();
            netTable = new JDBCNetworkUnifier();
        } catch(SQLException e) {}
        processConfig(config);
    }

    public NetworkAccess getNetwork(NetworkId network_id) throws Exception {
        NetworkDbObject[] tmpNetDbs = getSuccessfulNetworks();
        for(int i = 0; i < tmpNetDbs.length; i++) {
            if(NetworkIdUtil.areEqual(tmpNetDbs[i].getNetworkAccess()
                    .get_attributes()
                    .get_id(), network_id)) { return tmpNetDbs[i].getNetworkAccess(); }
        }
        throw new IllegalArgumentException("No network for id: "
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
    }

    public static final String[] PACKAGES = {"networkArm",
                                             "channel",
                                             "site",
                                             "station",
                                             "network"};

    private void loadConfigElement(Object sodElement) {
        if(sodElement instanceof edu.sc.seis.sod.subsetter.network.NetworkFinder) {
            finder = (edu.sc.seis.sod.subsetter.network.NetworkFinder)sodElement;
        } else if(sodElement instanceof NetworkSubsetter) {
            attrSubsetter = (NetworkSubsetter)sodElement;
        } else if(sodElement instanceof StationSubsetter) {
            stationSubsetter = (StationSubsetter)sodElement;
        } else if(sodElement instanceof SiteSubsetter) {
            siteSubsetter = (SiteSubsetter)sodElement;
        } else if(sodElement instanceof ChannelSubsetter) {
            channelSubsetter = (ChannelSubsetter)sodElement;
        } else if(sodElement instanceof NetworkProcess) {
            networkArmProcesses.add(sodElement);
        } else if(sodElement instanceof NetworkMonitor) {
            statusMonitors.add(sodElement);
        }
    }

    public void add(NetworkMonitor monitor) {
        statusMonitors.add(monitor);
    }

    public void processNetworkArm(NetworkAccess networkAccess, Channel channel)
            throws Exception {
        Iterator it = networkArmProcesses.iterator();
        while(it.hasNext()) {
            ((NetworkProcess)it.next()).process(networkAccess, channel);
        }
    }

    public ProxyNetworkDC getNetworkDC() {
        return finder.getNetworkDC();
    }

    private boolean needsRefresh() {
        TimeInterval refreshInterval = finder.getRefreshInterval();
        if(refreshInterval == null) return false;
        logger.debug("checking the refresh interval which is "
                + refreshInterval.getValue() + " " + refreshInterval.getUnit());
        MicroSecondDate databaseTime;
        try {
            databaseTime = queryTimeTable.getQuery(finder.getSourceName(),
                                                   finder.getDNSName());
            logger.debug("the last time the networks were checked was "
                         + databaseTime);
            MicroSecondDate lastTime = new MicroSecondDate(databaseTime);
            MicroSecondDate currentTime = ClockUtil.now();
            TimeInterval timeInterval = currentTime.difference(lastTime);
            timeInterval = (TimeInterval)timeInterval.convertTo(refreshInterval.getUnit());
            if(timeInterval.getValue() >= refreshInterval.getValue()) {
                return true;
            } else {
                statusChanged("Waiting until " + lastTime.add(refreshInterval)
                              + " to recheck networks");
                return false;
            }
        } catch(NotFound e) {
            logger.debug("The query database has no info about the network arm.  Hopefull this the first time through");
            return true;
        } catch(SQLException e) {
            GlobalExceptionHandler.handle("The query database threw this exception trying to find info about the network arm's current finder.  This bodes ill",
                                          e);
            return true;
        }
    }

    /**
     * returns an array of SuccessfulNetworks. if the refreshInterval is valid
     * it gets the networks from the database(may be embedded or external). if
     * not it gets the networks again from the networkserver. After obtaining
     * the Networks if processes them using the networkIdSubsetter and
     * networkAttrSubsetter and returns the succesful networks as an array of
     * NetworkDbObjects.
     * 
     * @return a <code>NetworkDbObject[]</code> value
     * @throws NotFound
     * @throws SQLException
     * @throws NetworkNotFound
     */
    public NetworkDbObject[] getSuccessfulNetworks() throws NetworkNotFound,
            SQLException, NotFound {
        if(!needsRefresh()) {
            if(netDbs == null) {
                netDbs = netTable.getAllNets(getNetworkDC());
                for(int i = 0; i < netDbs.length; i++) {
                    // this is for the side effect of creating
                    // networkInfoTemplate stuff
                    // avoids a null ptr later
                    change(netDbs[i].getNetworkAccess(),
                           Status.get(Stage.NETWORK_SUBSETTER, Standing.SUCCESS));
                }
            }
            return netDbs;
        }
        statusChanged("Getting networks");
        logger.debug("Getting NetworkDBObjects from network");
        ArrayList networkDBs = new ArrayList();
        NetworkDCOperations netDC = finder.getNetworkDC();
        NetworkAccess[] allNets;
        synchronized(netDC) {
            logger.debug("before netDC.a_finder().retrieve_all()");
            allNets = netDC.a_finder().retrieve_all();
        }
        logger.debug("found " + allNets.length
                + " network access objects from the network DC finder");
        for(int i = 0; i < allNets.length; i++) {
            try {
                allNets[i] = BulletproofVestFactory.vestNetworkAccess(allNets[i],
                                                                      BulletproofVestFactory.vestNetworkDC(finder.getDNSName(),
                                                                                                           finder.getSourceName(),
                                                                                                           finder.getFissuresNamingService()));
                if(attrSubsetter.accept(allNets[i].get_attributes())) {
                    int dbid;
                    synchronized(netTable) {
                        dbid = netTable.put(allNets[i].get_attributes());
                    }
                    networkDBs.add(new NetworkDbObject(dbid, allNets[i]));
                    change(allNets[i], Status.get(Stage.NETWORK_SUBSETTER,
                                                  Standing.SUCCESS));
                } else {
                    change(allNets[i], Status.get(Stage.NETWORK_SUBSETTER,
                                                  Standing.REJECT));
                }
            } catch(Throwable th) {
                GlobalExceptionHandler.handle("Got an exception while trying getSuccessfulNetworks for the "
                                                      + i + "th networkAccess",
                                              th);
            }
        }
        //Set the time of the last check to now
        queryTimeTable.setQuery(finder.getSourceName(),
                                finder.getDNSName(),
                                ClockUtil.now());
        netDbs = new NetworkDbObject[networkDBs.size()];
        networkDBs.toArray(netDbs);
        logger.debug("got " + netDbs.length + " networkDBobjects");
        statusChanged("Waiting for a request");
        return netDbs;
    }

    /**
     * Obtains the Stations corresponding to the given networkDbObject,
     * processes them using stationIdSubsetter and stationSubsetters, returns
     * the successful stations as an array of StationDbObjects.
     * 
     * @param networkDbObject
     *            a <code>NetworkDbObject</code> value
     * @return a <code>StationDbObject[]</code> value
     */
    public synchronized StationDbObject[] getSuccessfulStations(NetworkDbObject networkDbObject) {
        if(networkDbObject.stationDbObjects != null) { return networkDbObject.stationDbObjects; }
        statusChanged("Getting stations for "
                + networkDbObject.getNetworkAccess().get_attributes().name);
        ArrayList arrayList = new ArrayList();
        try {
            logger.debug("before NetworkAccess().retrieve_stations()");
            Station[] stations = networkDbObject.getNetworkAccess()
                    .retrieve_stations();
            logger.debug("after NetworkAccess().retrieve_stations()");
            for(int subCounter = 0; subCounter < stations.length; subCounter++) {
                if(stationSubsetter.accept(stations[subCounter])) {
                    int dbid;
                    synchronized(netTable) {
                        dbid = netTable.put(stations[subCounter]);
                    }
                    StationDbObject stationDbObject = new StationDbObject(dbid,
                                                                          stations[subCounter]);
                    arrayList.add(stationDbObject);
                    change(stations[subCounter],
                           Status.get(Stage.NETWORK_SUBSETTER, Standing.SUCCESS));
                } else {
                    change(stations[subCounter],
                           Status.get(Stage.NETWORK_SUBSETTER, Standing.REJECT));
                }
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem in method getSuccessfulStations for net "
                                                  + NetworkIdUtil.toString(networkDbObject.getNetworkAccess()
                                                          .get_attributes()
                                                          .get_id()),
                                          e);
        }
        StationDbObject[] rtnValues = new StationDbObject[arrayList.size()];
        rtnValues = (StationDbObject[])arrayList.toArray(rtnValues);
        networkDbObject.stationDbObjects = rtnValues;
        statusChanged("Waiting for a request");
        return rtnValues;
    }

    /**
     * a bit of a hack, because network status monitors get Stations and not
     * StationDBIds.
     */
    public int getStationDbId(Station station) throws SQLException {
        synchronized(netTable) {
            return netTable.put(station);
        }
    }

    /**
     * Obtains the Channels corresponding to the stationDbObject, retrievesthe
     * station from each channel and processes the channels using
     * SiteIdSubsetter and SiteSubsetter and returns an array of successful
     * SiteDbObjects.
     * 
     * @param networkDbObject
     *            a <code>NetworkDbObject</code> value
     * @param stationDbObject
     *            a <code>StationDbObject</code> value
     * @return a <code>SiteDbObject[]</code> value
     */
    public SiteDbObject[] getSuccessfulSites(NetworkDbObject networkDbObject,
                                             StationDbObject stationDbObject) {
        if(stationDbObject.siteDbObjects != null) { return stationDbObject.siteDbObjects; }
        statusChanged("Getting sites for "
                + stationDbObject.getStation().get_id().station_code);
        ArrayList successes = new ArrayList();
        List failures = new ArrayList();
        NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
        Station station = stationDbObject.getStation();
        try {
            logger.debug("before networkAccess.retrieve_for_station("
                    + station.get_id().network_id.network_code + "."
                    + station.get_id().station_code);
            Channel[] channels = networkAccess.retrieve_for_station(station.get_id());
            logger.debug("after networkAccess.retrieve_for_station("
                    + station.get_id().network_id.network_code + "."
                    + station.get_id().station_code);
            for(int i = 0; i < channels.length; i++) {
                if(siteSubsetter.accept(channels[i].my_site)) {
                    int dbid;
                    synchronized(netTable) {
                        dbid = netTable.put(channels[i].my_site);
                    }
                    SiteDbObject siteDbObject = new SiteDbObject(dbid,
                                                                 channels[i].my_site);
                    if(!containsSite(siteDbObject, successes)) {
                        successes.add(siteDbObject);
                        change(channels[i].my_site,
                               Status.get(Stage.NETWORK_SUBSETTER,
                                          Standing.SUCCESS));
                    }
                } else if(!failures.contains(channels[i].my_site)) {
                    change(channels[i].my_site,
                           Status.get(Stage.NETWORK_SUBSETTER, Standing.REJECT));
                    failures.add(channels[i].my_site);
                    // fail all channels in a failed site, just for status pages
                    change(channels[i], Status.get(Stage.NETWORK_SUBSETTER,
                                                   Standing.REJECT));
                }
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem in method getSuccessfulSites",
                                          e);
        }
        SiteDbObject[] rtnValues = new SiteDbObject[successes.size()];
        rtnValues = (SiteDbObject[])successes.toArray(rtnValues);
        stationDbObject.siteDbObjects = rtnValues;
        logger.debug("GOT " + rtnValues.length + " SITES");
        statusChanged("Waiting for a request");
        return rtnValues;
    }

    private boolean containsSite(SiteDbObject siteDbObject, ArrayList arrayList) {
        for(int counter = 0; counter < arrayList.size(); counter++) {
            SiteDbObject tempObject = (SiteDbObject)arrayList.get(counter);
            if(tempObject.getDbId() == siteDbObject.getDbId()) return true;
        }
        return false;
    }

    /**
     * Obtains the Channels corresponding to the siteDbObject, processes them
     * using the channelIdSubsetter and ChannelSubsetter and returns an array of
     * succesful ChannelDbObjects.
     * 
     * @param networkDbObject
     *            a <code>NetworkDbObject</code> value
     * @param siteDbObject
     *            a <code>SiteDbObject</code> value
     * @return a <code>ChannelDbObject[]</code> value
     */
    public ChannelDbObject[] getSuccessfulChannels(NetworkDbObject networkDbObject,
                                                   SiteDbObject siteDbObject) {
        if(siteDbObject.channelDbObjects != null) { return siteDbObject.channelDbObjects; }
        statusChanged("Getting channels for " + siteDbObject);
        List successes = new ArrayList();
        NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
        Site site = siteDbObject.getSite();
        try {
            Channel[] channels = networkAccess.retrieve_for_station(site.my_station.get_id());
            JDBCChannel chanDb = netTable.getChannelDb();
            Status inProg = Status.get(Stage.NETWORK_SUBSETTER,
                                       Standing.IN_PROG);
            for(int i = 0; i < channels.length; i++) {
                Channel chan = channels[i];
                if(!isSameSite(site, chan.my_site)) {
                    continue;
                }
                change(chan, inProg);
                if(channelSubsetter.accept(chan)) {
                    int dbid;
                    synchronized(netTable) {
                        try {
                            dbid = chanDb.getDBId(chan.get_id());
                        } catch(NotFound e) {
                            dbid = netTable.put(chan);
                        }
                    }
                    channelMap.put(new Integer(dbid), chan);
                    ChannelDbObject channelDbObject = new ChannelDbObject(dbid,
                                                                          chan);
                    successes.add(channelDbObject);
                    change(chan, Status.get(Stage.PROCESSOR, Standing.IN_PROG));
                    processNetworkArm(networkAccess, chan);
                    change(chan, Status.get(Stage.NETWORK_SUBSETTER,
                                            Standing.SUCCESS));
                } else change(chan, Status.get(Stage.NETWORK_SUBSETTER,
                                               Standing.REJECT));
            }
        } catch(Throwable e) {
            GlobalExceptionHandler.handle("Problem in method getSuccessfulChannels for "
                                                  + StationIdUtil.toString(site.my_station.get_id()),
                                          e);
        }
        ChannelDbObject[] values = new ChannelDbObject[successes.size()];
        values = (ChannelDbObject[])successes.toArray(values);
        siteDbObject.channelDbObjects = values;
        logger.debug("got " + values.length + " channels");
        statusChanged("Waiting for a request");
        return values;
    }

    //This is a HACK. Since we're already storing the channels whole hog, it
    //isn't much of a stretch to cache them by dbid, and this allows
    //JDBCEventChannelStatus to quickly pull them out instead of going to the
    //Net database
    public Channel getChannel(int chanId) {
        return (Channel)channelMap.get(new Integer(chanId));
    }

    private Map channelMap = Collections.synchronizedMap(new HashMap());

    private void statusChanged(String newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()) {
            try {
                ((NetworkMonitor)it.next()).setArmStatus(newStatus);
            } catch(Exception e) {
                // caught for one, but should continue with rest after logging
                // it
                GlobalExceptionHandler.handle("Problem changing status in NetworkArm",
                                              e);
            }
        }
    }

    private void change(Channel chan, Status newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()) {
            try {
                ((NetworkMonitor)it.next()).change(chan, newStatus);
            } catch(Throwable e) {
                // caught for one, but should continue with rest after logging
                // it
                GlobalExceptionHandler.handle("Problem changing channel status in NetworkArm",
                                              e);
            }
        }
    }

    private void change(Station sta, Status newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()) {
            try {
                ((NetworkMonitor)it.next()).change(sta, newStatus);
            } catch(Exception e) {
                // caught for one, but should continue with rest after logging
                // it
                GlobalExceptionHandler.handle("Problem changing station status in NetworkArm",
                                              e);
            }
        }
    }

    private void change(NetworkAccess na, Status newStatus) {
        //try{
        //    networkStatusTable.setStatus(na.get_attributes(), newStatus);
        //}
        //catch(SQLException e){
        //    GlobalExceptionHandler.handle("something wrong changing status in
        // database", e);
        //}
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()) {
            try {
                ((NetworkMonitor)it.next()).change(na, newStatus);
            } catch(Throwable e) {
                // caught for one, but should continue with rest after logging
                // it
                GlobalExceptionHandler.handle("Problem changing network status in NetworkArm",
                                              e);
            }
        }
    }

    private void change(Site site, Status newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()) {
            try {
                ((NetworkMonitor)it.next()).change(site, newStatus);
            } catch(Throwable e) {
                // caught for one, but should continue with rest after logging
                // it
                GlobalExceptionHandler.handle("Problem changing site status in NetworkArm",
                                              e);
            }
        }
    }

    private boolean isSameSite(Site givenSite, Site tempSite) {
        SiteId givenSiteId = givenSite.get_id();
        SiteId tempSiteId = tempSite.get_id();
        if(givenSiteId.site_code.equals(tempSiteId.site_code)) {
            MicroSecondDate givenDate = new MicroSecondDate(givenSiteId.begin_time);
            MicroSecondDate tempDate = new MicroSecondDate(tempSiteId.begin_time);
            if(tempDate.equals(givenDate)) return true;
        }
        return false;
    }

    private edu.sc.seis.sod.subsetter.network.NetworkFinder finder = null;

    private NetworkSubsetter attrSubsetter = new PassNetwork();

    private StationSubsetter stationSubsetter = new PassStation();

    private SiteSubsetter siteSubsetter = new PassSite();

    private ChannelSubsetter channelSubsetter = new PassChannel();

    private List networkArmProcesses = new ArrayList();

    private JDBCQueryTime queryTimeTable;

    private JDBCNetworkUnifier netTable;

    //private JDBCNetworkStatus networkStatusTable;w
    private NetworkDbObject[] netDbs;

    private List statusMonitors = new ArrayList();

    private static Logger logger = Logger.getLogger(NetworkArm.class);
}// NetworkArm
