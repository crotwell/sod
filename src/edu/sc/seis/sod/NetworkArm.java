package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.sc.seis.sod.database.*;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.RetryNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.RetryNetworkDC;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.subsetter.RefreshInterval;
import edu.sc.seis.sod.subsetter.networkArm.NullChannelSubsetter;
import edu.sc.seis.sod.subsetter.networkArm.NullNetworkSubsetter;
import edu.sc.seis.sod.subsetter.networkArm.NullSiteSubsetter;
import edu.sc.seis.sod.subsetter.networkArm.NullStationSubsetter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the subsetting of the Channels.
 *
 *
 * Created: Wed Mar 20 13:30:06 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class NetworkArm {
    /**
     * Creates a new <code>NetworkArm</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public NetworkArm (Element config) throws ConfigurationException {
        if ( ! config.getTagName().equals("networkArm")) {
            throw new IllegalArgumentException("Configuration element must be a NetworkArm tag");
        }
        processConfig(config);
    }
    
    /**
     * Describe <code>processConfig</code> method here.
     *
     * @exception ConfigurationException if an error occurs
     */
    private void processConfig(Element config)
        throws ConfigurationException {
        networkDatabase = DatabaseManager.getDatabaseManager(Start.getProperties(), "postgres").getNetworkDatabase();
        
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                
                Object sodElement =
                    SodUtil.load((Element)node,
                                 "edu.sc.seis.sod.subsetter.networkArm");
                loadConfigElement(sodElement);
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }
    
    void loadConfigElement(Object sodElement) throws ConfigurationException {
        if(sodElement instanceof edu.sc.seis.sod.subsetter.networkArm.NetworkFinder) {
            finderSubsetter =
                (edu.sc.seis.sod.subsetter.networkArm.NetworkFinder)sodElement;
        } else if(sodElement instanceof NetworkSubsetter) {
            if (attrSubsetter instanceof NullNetworkSubsetter ) {
                attrSubsetter = (NetworkSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one NetworkAttrSubsetter is in the configuration file: "+sodElement);
            } // end of else
        } else if(sodElement instanceof StationSubsetter) {
            if ( stationSubsetter instanceof NullStationSubsetter ) {
                stationSubsetter = (StationSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one StationSubsetter is in the configuration file: "+sodElement);
            } // end of else
            
        } else if(sodElement instanceof SiteSubsetter) {
            if ( siteSubsetter instanceof NullSiteSubsetter ) {
                siteSubsetter = (SiteSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one SiteSubsetter is in the configuration file: "+sodElement);
            } // end of else
            
        } else if(sodElement instanceof ChannelSubsetter) {
            if ( channelSubsetter instanceof NullChannelSubsetter ) {
                channelSubsetter = (ChannelSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one ChannelSubsetter is in the configuration file: "+sodElement);
            } // end of else
            
        } else if(sodElement instanceof NetworkArmProcess) {
            networkArmProcesses.add(sodElement);
        }else if(sodElement instanceof NetworkStatus) {
            statusMonitors.add(sodElement);
        }
    }
    
    public void processNetworkArm(NetworkAccess networkAccess,
                                  Channel channel,
                                  CookieJar cookieJar) throws Exception{
        Iterator it = networkArmProcesses.iterator();
        while(it.hasNext()){
            ((NetworkArmProcess)it.next()).process(networkAccess, channel, cookieJar);
        }
    }
    
    public synchronized  Channel getChannel(int dbid) {
        return networkDatabase.getChannel(dbid);
    }
    
    public synchronized NetworkAccess getNetworkAccess(int dbid) {
        return networkDatabase.getNetworkAccess(dbid);
    }
    
    public synchronized int getSiteDbId(int channelid) {
        return networkDatabase.getSiteDbId(channelid);
    }
    
    public synchronized int getStationDbId(int siteid) {
        return networkDatabase.getStationDbId(siteid);
    }
    
    public synchronized int getNetworkDbId(int stationid) {
        return networkDatabase.getNetworkDbId(stationid);
    }
    
    /**
     * checks if the refresh interval specified in the configuration of the
     * NetworkFinder has passed since the last time networks were checked
     * according to the database.
     *
     * @returns true if the number of minutes se
     */
    private boolean needsRefresh() {
        RefreshInterval refreshInterval = finderSubsetter.getRefreshInterval();
        try {
            logger.debug("checking on the validity of the refresh interval which is " + refreshInterval.getValue() + " " + refreshInterval.getUnit());
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Time databaseTime = networkDatabase.getTime(finderSubsetter.getSourceName(),
                                                    finderSubsetter.getDNSName());
        logger.debug("the last time the networks were checked was " + new MicroSecondDate(databaseTime));
        if(refreshInterval == null) return false;
        MicroSecondDate lastTime = new MicroSecondDate(databaseTime);
        MicroSecondDate currentTime = ClockUtil.now();
        TimeInterval timeInterval = currentTime.difference(lastTime);
        try {
            timeInterval = (TimeInterval)timeInterval.convertTo(refreshInterval.getUnit());
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("The time interval set in the refreshInterval for the NetworkFinder has an unacceptable unit");
            System.exit(0);
        }
        if(timeInterval.getValue() >= refreshInterval.getValue()) return true;
        try {
            statusChanged("Waiting until " + lastTime.add(refreshInterval.getTimeInterval()) + " to recheck networks");
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("The time interval set in the refreshInterval for the NetworkFinder has an unacceptable unit");
            System.exit(0);
        }
        return false;
    }
    
    /**
     * returns an array of SuccessfulNetworks.
     * if the refreshInterval is valid it gets the networks from the database(may be embedded or external).
     * if not it gets the networks again from the networkserver. After obtaining the Networks if processes them
     * using the networkIdSubsetter and networkAttrSubsetter and returns the succesful networks as an array of
     * NetworkDbObjects.
     *
     * @return a <code>NetworkDbObject[]</code> value
     * @exception Exception if an error occurs
     */
    public NetworkDbObject[] getSuccessfulNetworks() throws Exception {
        if(networksBeenChecked && !needsRefresh()) {
            return networkDbObjects;
        }
        statusChanged("Getting networks");
        logger.debug("Getting NetworkDBObjects from network");
        ArrayList networkDBs = new ArrayList();
        NetworkDCOperations netDC =
            new RetryNetworkDC(finderSubsetter.getNetworkDC(), 2);
        NetworkAccess[] allNets = netDC.a_finder().retrieve_all();
        logger.debug("found " + allNets.length + " network access objects from the network DC finder");
        for(int i = 0; i < allNets.length; i++) {
            if (allNets[i] != null) {
                CookieJar cookieJar = new CookieJar();
                cookieJarCache.put(allNets[i], cookieJar);
                allNets[i] = new RetryNetworkAccess(allNets[i], 2);
                if(attrSubsetter.accept(allNets[i].get_attributes(),cookieJar)){
                    int dbid = networkDatabase.putNetwork(finderSubsetter.getSourceName(),
                                                          finderSubsetter.getDNSName(),
                                                          allNets[i]);
                    networkDBs.add(new NetworkDbObject(dbid, allNets[i]));
                }else{
                    change(allNets[i], RunStatus.FAILED);
                }
            } // end of if (allNets[counter] != null)
        }
        //Set the time of the last check to now
        networkDatabase.setTime(finderSubsetter.getSourceName(),
                                finderSubsetter.getDNSName(),
                                ClockUtil.now().getFissuresTime());
        
        networkDbObjects = new NetworkDbObject[networkDBs.size()];
        networkDbObjects = (NetworkDbObject[]) networkDBs.toArray(networkDbObjects);
        logger.debug("got " + networkDbObjects.length + " networkDBobjects");
        networksBeenChecked = true;
        statusChanged("Waiting for a request");
        for (int i = 0; i < networkDbObjects.length; i++) {
            change(networkDbObjects[i].getNetworkAccess(), RunStatus.PASSED);
        }
        return networkDbObjects;
    }
    
    /**
     * Obtains the Stations corresponding to the given networkDbObject, processes them
     * using stationIdSubsetter and stationSubsetters, returns the successful stations as
     * an array of StationDbObjects.
     *
     * @param networkDbObject a <code>NetworkDbObject</code> value
     * @return a <code>StationDbObject[]</code> value
     */
    public StationDbObject[] getSuccessfulStations(NetworkDbObject networkDbObject) {
        if(networkDbObject.stationDbObjects != null) {
            return networkDbObject.stationDbObjects;
        }
        statusChanged("Getting stations for " + networkDbObject.getNetworkAccess().get_attributes().name);
        ArrayList arrayList = new ArrayList();
        try {
            CookieJar cookieJar =
                (CookieJar)cookieJarCache.get(networkDbObject.getNetworkAccess());
            Station[] stations = networkDbObject.getNetworkAccess().retrieve_stations();
            for(int subCounter = 0; subCounter < stations.length; subCounter++) {
                if(stationSubsetter.accept(networkDbObject.getNetworkAccess(), stations[subCounter], cookieJar)) {
                    int dbid = networkDatabase.putStation(networkDbObject, stations[subCounter]);
                    StationDbObject stationDbObject = new StationDbObject(dbid, stations[subCounter]);
                    arrayList.add(stationDbObject);
                }else{
                    change(stations[subCounter], RunStatus.FAILED);
                }
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        StationDbObject[] rtnValues = new StationDbObject[arrayList.size()];
        rtnValues = (StationDbObject[]) arrayList.toArray(rtnValues);
        networkDbObject.stationDbObjects = rtnValues;
        statusChanged("Waiting for a request");
        for (int i = 0; i < rtnValues.length; i++) {
            change(rtnValues[i].getStation(), RunStatus.PASSED);
        }
        return rtnValues;
    }
    
    /**
     * Obtains the Channels corresponding to the stationDbObject, retrievesthe station from each channel
     * and processes the channels using SiteIdSubsetter and SiteSubsetter  and returns an array of
     * successful SiteDbObjects.
     * @param networkDbObject a <code>NetworkDbObject</code> value
     * @param stationDbObject a <code>StationDbObject</code> value
     * @return a <code>SiteDbObject[]</code> value
     */
    public SiteDbObject[] getSuccessfulSites(NetworkDbObject networkDbObject, StationDbObject stationDbObject) {
        if(stationDbObject.siteDbObjects != null) {
            return stationDbObject.siteDbObjects;
        }
        statusChanged("Getting sites for " + stationDbObject.getStation().get_id().station_code);
        ArrayList successes = new ArrayList();
        List failures = new ArrayList();
        NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
        Station station = stationDbObject.getStation();
        try {
            Channel[] channels = networkAccess.retrieve_for_station(station.get_id());
            for(int i = 0; i < channels.length; i++) {
                if(siteSubsetter.accept(networkAccess, channels[i].my_site, null)) {
                    int dbid = networkDatabase.putSite(stationDbObject,
                                                       channels[i].my_site);
                    SiteDbObject siteDbObject = new SiteDbObject(dbid,
                                                                 channels[i].my_site);
                    if(!containsSite(siteDbObject, successes)) {
                        successes.add(siteDbObject);
                    }
                }else if(!failures.contains(channels[i].my_site)){
                    change(channels[i].my_site, RunStatus.FAILED);
                    failures.add(channels[i].my_site);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        SiteDbObject[] rtnValues = new SiteDbObject[successes.size()];
        rtnValues = (SiteDbObject[]) successes.toArray(rtnValues);
        stationDbObject.siteDbObjects = rtnValues;
        logger.debug(" THE LENFGHT OF THE SITES IS ***************** "+rtnValues.length);
        statusChanged("Waiting for a request");
        for (int i = 0; i < rtnValues.length; i++) {
            change(rtnValues[i].getSite(), RunStatus.PASSED);
        }
        return rtnValues;
        
    }
    
    private boolean containsSite(SiteDbObject siteDbObject, ArrayList arrayList) {
        for(int counter = 0; counter < arrayList.size(); counter++) {
            SiteDbObject tempObject = (SiteDbObject) arrayList.get(counter);
            if(tempObject.getDbId() == siteDbObject.getDbId()) return true;
        }
        return false;
    }
    
    
    
    /**
     * Obtains the Channels corresponding to the siteDbObject, processes them using the
     * channelIdSubsetter and ChannelSubsetter and returns an array of succesful ChannelDbObjects.
     *
     * @param networkDbObject a <code>NetworkDbObject</code> value
     * @param siteDbObject a <code>SiteDbObject</code> value
     * @return a <code>ChannelDbObject[]</code> value
     */
    public ChannelDbObject[] getSuccessfulChannels(NetworkDbObject networkDbObject, SiteDbObject siteDbObject) {
        if(siteDbObject.channelDbObjects != null) {
            return siteDbObject.channelDbObjects;
        }
        statusChanged("Getting channels for " + siteDbObject);
        List successes = new ArrayList();
        NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
        CookieJar cookieJar = (CookieJar)cookieJarCache.get(networkAccess);
        
        Site site = siteDbObject.getSite();
        try {
            Channel[] channels = networkAccess.retrieve_for_station(site.my_station.get_id());
            
            for(int subCounter = 0; subCounter < channels.length; subCounter++) {
                change(channels[subCounter], RunStatus.NEW);
                if(!isSameSite(site, channels[subCounter].my_site)){
                    continue;
                }else if(channelSubsetter.accept(networkAccess, channels[subCounter], null)) {
                    int dbid = networkDatabase.putChannel(siteDbObject,
                                                          channels[subCounter]);
                    ChannelDbObject channelDbObject = new ChannelDbObject(dbid,
                                                                          channels[subCounter]);
                    successes.add(channelDbObject);
                    processNetworkArm(networkAccess, channels[subCounter], cookieJar);
                }else change(channels[subCounter], RunStatus.FAILED);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        ChannelDbObject[] values = new ChannelDbObject[successes.size()];
        values = (ChannelDbObject[]) successes.toArray(values);
        siteDbObject.channelDbObjects = values;
        logger.debug("got "+values.length+" channels");
        statusChanged("Waiting for a request");
        for (int i = 0; i < values.length; i++) {
            change(values[i].getChannel(), RunStatus.PASSED);
        }
        return values;
    }
    
    private void statusChanged(String newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((NetworkStatus)it.next()).setArmStatus(newStatus);
        }
    }
    
    private void change(Channel chan, RunStatus newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((NetworkStatus)it.next()).change(chan, newStatus);
        }
    }
    
    private void change(Station sta, RunStatus newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((NetworkStatus)it.next()).change(sta, newStatus);
        }
    }
    
    private void change(NetworkAccess na, RunStatus newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((NetworkStatus)it.next()).change(na, newStatus);
        }
    }
    
    private void change(Site site, RunStatus newStatus) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((NetworkStatus)it.next()).change(site, newStatus);
        }
    }
    
    private boolean isSameSite(Site givenSite, Site tempSite) {
        SiteId givenSiteId = givenSite.get_id();
        SiteId tempSiteId = tempSite.get_id();
        if(givenSiteId.site_code.equals(tempSiteId.site_code)) {
            MicroSecondDate givenDate = new MicroSecondDate(givenSiteId.begin_time);
            MicroSecondDate tempDate = new  MicroSecondDate(tempSiteId.begin_time);
            if(tempDate.equals(givenDate)) return true;
        }
        return false;
    }
    
    
    private edu.sc.seis.sod.subsetter.networkArm.NetworkFinder finderSubsetter = null;
    
    private NetworkSubsetter attrSubsetter = new NullNetworkSubsetter();
    
    private StationSubsetter stationSubsetter = new NullStationSubsetter();
    
    private SiteSubsetter siteSubsetter = new NullSiteSubsetter();
    
    private ChannelSubsetter channelSubsetter = new NullChannelSubsetter();
    
    private List networkArmProcesses = new ArrayList();
    
    //Set to true the first time getSuccessfulNetworks is called
    private boolean networksBeenChecked = false;
    
    private NetworkDbObject[] networkDbObjects;
    
    private NetworkDatabase networkDatabase;
    
    private HashMap cookieJarCache = new HashMap();
    
    private List statusMonitors = new ArrayList();
    
    private String status;
    
    private static Logger logger = Logger.getLogger(NetworkArm.class);
    
    static Logger failure = Logger.getLogger(NetworkArm.class.getName()+".failure");
}// NetworkArm
