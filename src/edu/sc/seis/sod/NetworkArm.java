package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.database.*;

import org.w3c.dom.*;
import org.apache.log4j.*;

import edu.sc.seis.sod.subsetter.networkArm.*;
import edu.sc.seis.fissuresUtil.chooser.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;

import java.util.*;
import edu.sc.seis.fissuresUtil.cache.RetryNetworkDC;
import edu.sc.seis.fissuresUtil.cache.RetryNetworkAccess;

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
        this.config = config;
        processConfig();
        //  processConfig(config);
    }

    /**
     * Describe <code>processConfig</code> method here.
     *
     * @exception ConfigurationException if an error occurs
     */
    protected void processConfig()
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
            networkFinderSubsetter =
                (edu.sc.seis.sod.subsetter.networkArm.NetworkFinder)sodElement;
        } else if(sodElement instanceof NetworkIdSubsetter) {
            if (networkIdSubsetter instanceof NullNetworkIdSubsetter ) {
                networkIdSubsetter = (NetworkIdSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one NetworkIdSubsetter is in the configuration file: "+sodElement);
            } // end of else
        } else if(sodElement instanceof NetworkAttrSubsetter) {
            if (networkAttrSubsetter instanceof NullNetworkAttrSubsetter ) {
                networkAttrSubsetter = (NetworkAttrSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one NetworkAttrSubsetter is in the configuration file: "+sodElement);
            } // end of else

        } else if (sodElement instanceof StationIdSubsetter) {
            if ( stationIdSubsetter instanceof NullStationIdSubsetter ) {
                stationIdSubsetter = (StationIdSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one StationIdSubsetter is in the configuration file: "+sodElement);
            } // end of else

        } else if(sodElement instanceof StationSubsetter) {
            if ( stationSubsetter instanceof NullStationSubsetter ) {
                stationSubsetter = (StationSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one StationSubsetter is in the configuration file: "+sodElement);
            } // end of else

        } else if(sodElement instanceof SiteIdSubsetter) {
            if ( siteIdSubsetter instanceof NullSiteIdSubsetter ) {
                siteIdSubsetter = (SiteIdSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one SiteIdSubsetter is in the configuration file: "+sodElement);
            } // end of else

        } else if(sodElement instanceof SiteSubsetter) {
            if ( siteSubsetter instanceof NullSiteSubsetter ) {
                siteSubsetter = (SiteSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one SiteSubsetter is in the configuration file: "+sodElement);
            } // end of else

        } else if(sodElement instanceof ChannelIdSubsetter) {
            if ( channelIdSubsetter instanceof NullChannelIdSubsetter ) {
                channelIdSubsetter = (ChannelIdSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one ChannelIdSubsetter is in the configuration file: "+sodElement);
            } // end of else

        } else if(sodElement instanceof ChannelSubsetter) {
            if ( channelSubsetter instanceof NullChannelSubsetter ) {
                channelSubsetter = (ChannelSubsetter)sodElement;
            } else {
                throw new ConfigurationException("More than one ChannelSubsetter is in the configuration file: "+sodElement);
            } // end of else

        } else if(sodElement instanceof NetworkArmProcess) {
            if ( networkArmProcess[0] instanceof NullNetworkProcess) {
                networkArmProcess[0] = (NetworkArmProcess)sodElement;
            } else {
                // must already be a process, add to array
                NetworkArmProcess[] tmp =
                    new NetworkArmProcess[networkArmProcess.length+1];
                System.arraycopy(networkArmProcess, 0, tmp, 0, networkArmProcess.length);
                tmp[tmp.length-1] = (NetworkArmProcess)sodElement;
                networkArmProcess = tmp;
            } // end of else
        } else if(sodElement instanceof NetworkStatusProcessor) {
            if ( networkStatusProcess[0] instanceof NullNetworkStatusProcessor) {
                networkStatusProcess[0] = (NetworkStatusProcessor)sodElement;
            } else {
                // must already be a process, add to array
                NetworkStatusProcessor[] tmp =
                    new NetworkStatusProcessor[networkStatusProcess.length+1];
                System.arraycopy(networkStatusProcess, 0, tmp, 0, networkStatusProcess.length);
                tmp[tmp.length-1] = (NetworkStatusProcessor)sodElement;
                networkStatusProcess = tmp;
            } // end of else
        }
    }

    /*
     *This function starts the processing of the network Arm.
     *It gets all the NetworkAccesses and network ids from them
     *and checks if the networkId is accepted by the networkIdSubsetter.
     **/

    /**
     * Describe <code>processNetworkArm</code> method here.
     *
     * @exception Exception if an error occurs
     */
    public void processNetworkArm() throws Exception{
        channelList = new ArrayList();
        NetworkDC netdc = networkFinderSubsetter.getNetworkDC();
        finder = netdc.a_finder();
        edu.iris.Fissures.IfNetwork.NetworkAccess[] allNets = finder.retrieve_all();
        logger.info("Got "+allNets.length+" networks from finder.");
        networkIds = new NetworkId[allNets.length];
        for(int counter = 0; counter < allNets.length; counter++) {
            if (allNets[counter] != null) {
                CookieJar cookieJar = null;

                NetworkAttr attr = allNets[counter].get_attributes();
                networkIds[counter] = attr.get_id();
                if(networkIdSubsetter.accept(networkIds[counter], cookieJar)) {
                    for ( int i=0; i<networkStatusProcess.length; i++) {
                        networkStatusProcess[i].networkId(true,
                                                          networkIds[counter],
                                                          cookieJar);
                    } // end of for ()
                    handleNetworkAttrSubsetter(allNets[counter], attr, cookieJar);

                } else {
                    for ( int i=0; i<networkStatusProcess.length; i++) {
                        networkStatusProcess[i].networkId(false,
                                                          networkIds[counter],
                                                          cookieJar);
                    } // end of for ()

                    failure.info("Fail "+attr.get_code());
                } // end of else

            } // end of if (allNets[counter] != null)

        }
        successfulChannels = new Channel[channelList.size()];
        successfulChannels = (Channel[]) channelList.toArray(successfulChannels);
    }

    /**
     * handles the networkAttrSubsetter
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param networkAttr a <code>NetworkAttr</code> value
     * @exception Exception if an error occurs
     */
    public void handleNetworkAttrSubsetter(NetworkAccess networkAccess, NetworkAttr networkAttr, CookieJar cookieJar) throws Exception{
        try {
            //logger.debug("The stationIdSubsetter is not null");
            if(networkAttrSubsetter.accept(networkAttr, cookieJar)) {
                for ( int i=0; i<networkStatusProcess.length; i++) {
                    networkStatusProcess[i].networkAttr(true,
                                                        networkAttr,
                                                        cookieJar);
                } // end of for ()
                Station[] stations = networkAccess.retrieve_stations();
                for(int subCounter = 0; subCounter < stations.length; subCounter++) {
                    handleStationIdSubsetter(networkAccess,
                                             stations[subCounter],
                                             cookieJar);
                }
            } else {
                for ( int i=0; i<networkStatusProcess.length; i++) {
                    networkStatusProcess[i].networkAttr(false,
                                                        networkAttr,
                                                        cookieJar);
                } // end of for ()
                failure.info("Fail NetworkAttr"+networkAttr.get_code());
            }
        } catch (org.omg.CORBA.UNKNOWN e) {
            logger.warn("Caught exception, network is: "+NetworkIdUtil.toString(networkAttr.get_id()), e);
            throw e;
        } // end of try-catch

    }

    /**
     * handles the stationIdSubsetter
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @exception Exception if an error occurs
     */
    public void handleStationIdSubsetter(NetworkAccess networkAccess, Station  station, CookieJar cookieJar) throws Exception{
        try {

            if(stationIdSubsetter.accept(station.get_id(), cookieJar)) {
                for ( int i=0; i<networkStatusProcess.length; i++) {
                    networkStatusProcess[i].stationId(true,
                                                      station.get_id(),
                                                      cookieJar);
                } // end of for ()

                handleStationSubsetter(networkAccess, station, cookieJar);
            } else {
                for ( int i=0; i<networkStatusProcess.length; i++) {
                    networkStatusProcess[i].stationId(false,
                                                      station.get_id(),
                                                      cookieJar);
                } // end of for ()
                failure.info("Fail StationId"+station.get_code());
            }
        } catch (org.omg.CORBA.UNKNOWN e) {
            logger.warn("Caught exception, station is: "+StationIdUtil.toString(station.get_id()), e);
            throw e;
        } // end of try-catch

    }

    /**
     * handles the stationSubsetter
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @exception Exception if an error occurs
     */
    public void handleStationSubsetter(NetworkAccess networkAccess, Station station, CookieJar cookieJar) throws Exception{

        if(stationSubsetter.accept(networkAccess, station, cookieJar)) {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].station(true,
                                                networkAccess,
                                                station,
                                                cookieJar);
            } // end of for ()

            Channel[] channels = null;
            try {
                channels =
                    networkAccess.retrieve_for_station(station.get_id());
            } catch (org.omg.CORBA.UNKNOWN e) {
                logger.warn("Caught an UNKNOWN station id ="+StationIdUtil.toString(station.get_id()), e);
                throw e;
            } // end of try-catch

            for(int subCounter = 0; subCounter < channels.length; subCounter++) {
                handleSiteIdSubsetter(networkAccess, channels[subCounter], cookieJar);


            }
        } else {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].station(false,
                                                networkAccess,
                                                station,
                                                cookieJar);
            } // end of for ()
            failure.info("Fail Station"+station.get_code());
        }
    }

    /**
     * handles the siteIdSubsetter
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleSiteIdSubsetter(NetworkAccess networkAccess, Channel channel, CookieJar cookieJar) throws Exception{

        if(siteIdSubsetter.accept(channel.my_site.get_id(), cookieJar)) {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].siteId(true,
                                               channel.my_site.get_id(),
                                               cookieJar);
            } // end of for ()

            handleSiteSubsetter(networkAccess, channel, cookieJar);
        } else {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].siteId(false,
                                               channel.my_site.get_id(),
                                               cookieJar);
            } // end of for ()
            failure.info("Fail SiteId "+SiteIdUtil.toString(channel.my_site.get_id()));
        }
    }

    /**
     * handles the siteSubsetter
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleSiteSubsetter(NetworkAccess networkAccess, Channel channel, CookieJar cookieJar) throws Exception{

        if(siteSubsetter.accept(networkAccess, channel.my_site, cookieJar)) {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].site(true,
                                             networkAccess,
                                             channel.my_site,
                                             cookieJar);
            } // end of for ()

            handleChannelIdSubsetter(networkAccess, channel, cookieJar);
        } else {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].site(false,
                                             networkAccess,
                                             channel.my_site,
                                             cookieJar);
            } // end of for ()

            failure.info("Fail Site "+SiteIdUtil.toString(channel.my_site.get_id()));
        }
    }

    /**
     * handles the channelIdSubsetter
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleChannelIdSubsetter(NetworkAccess networkAccess, Channel channel, CookieJar cookieJar) throws Exception{

        if(channelIdSubsetter.accept(channel.get_id(), cookieJar)) {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].channelId(true,
                                                  channel.get_id(),
                                                  cookieJar);
            } // end of for ()

            handleChannelSubsetter(networkAccess, channel, cookieJar);
        } else {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].channelId(false,
                                                  channel.get_id(),
                                                  cookieJar);
            } // end of for ()

            failure.info("Fail ChannelId"+ChannelIdUtil.toStringNoDates(channel.get_id()));
        }

    }

    /**
     * handles the channelSubsetter.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleChannelSubsetter(NetworkAccess networkAccess, Channel channel, CookieJar cookieJar) throws Exception{

        if(channelSubsetter.accept(networkAccess, channel, cookieJar)) {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].channel(true,
                                                networkAccess,
                                                channel,
                                                cookieJar);
            } // end of for ()

            handleNetworkArmProcess(networkAccess, channel, cookieJar);
        } else {
            for ( int i=0; i<networkStatusProcess.length; i++) {
                networkStatusProcess[i].channel(false,
                                                networkAccess,
                                                channel,
                                                cookieJar);
            } // end of for ()

            failure.info("Fail Channel "+ChannelIdUtil.toStringNoDates(channel.get_id()));
        }
    }

    /**
     * handles the networkArmProcess.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleNetworkArmProcess(NetworkAccess networkAccess,
                                        Channel channel,
                                        CookieJar cookieJar) throws Exception{
        for ( int i=0; i<networkArmProcess.length; i++) {
            networkArmProcess[i].process(networkAccess, channel, cookieJar);
        } // end of for ()
    }


    /**
     * returns the Channel corresponding to the databaseid dbid.
     *
     * @param dbid an <code>int</code> value
     * @return a <code>Channel</code> value
     */
    public synchronized  Channel getChannel(int dbid) {
        return networkDatabase.getChannel(dbid);
    }

    /**
     * returns the ObjectReference of the NetworkAcces corresponding to the databaseid dbid.
     *
     * @param dbid an <code>int</code> value
     * @return a <code>NetworkAccess</code> value
     */
    public synchronized NetworkAccess getNetworkAccess(int dbid) {
        return networkDatabase.getNetworkAccess(dbid);
    }

    /**
     * returns the sitedatabaseid corresponding to the channelid.
     *
     * @param channelid an <code>int</code> value
     * @return an <code>int</code> value
     */
    public synchronized int getSiteDbId(int channelid) {
        return networkDatabase.getSiteDbId(channelid);
    }

    /**
     * returns the stationdatabaseid corresponding to the siteid
     *
     * @param siteid an <code>int</code> value
     * @return an <code>int</code> value
     */
    public synchronized int getStationDbId(int siteid) {
        return networkDatabase.getStationDbId(siteid);
    }

    /**
     * returns the networkid corresponding to the stationid.
     *
     * @param stationid an <code>int</code> value
     * @return an <code>int</code> value
     */
    public synchronized int getNetworkDbId(int stationid) {
        return networkDatabase.getNetworkDbId(stationid);
    }

    /**
     * checks if the refreshInterval specified inthe property file is still valid at the currentTime.
     * If the (currenttime  - lasttimeofnetworkquery) is greater than refreshInteval it retuns true.
     * else returns false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isRefreshIntervalValid() {
        RefreshInterval refreshInterval = networkFinderSubsetter.getRefreshInterval();
        edu.iris.Fissures.Time databaseTime = networkDatabase.getTime(networkFinderSubsetter.getSourceName(),
                                                                      networkFinderSubsetter.getDNSName());


        //if the networktimeconfig is null or
        //at every time of restart or startup
        //get the networks over the net.
        if(databaseTime == null || lastDate == null) return false;
        if(refreshInterval == null) return true;

        Date currentDate = Calendar.getInstance().getTime();
        MicroSecondDate lastTime = new MicroSecondDate(databaseTime);
        MicroSecondDate currentTime = new MicroSecondDate(databaseTime);
        TimeInterval timeInterval = currentTime.difference(lastTime);
        timeInterval = (TimeInterval)timeInterval.convertTo(UnitImpl.MINUTE);
        int minutes = (int)timeInterval.value;
        if(minutes <= refreshInterval.getValue()) {
            return true;
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
        if(isRefreshIntervalValid()) {
            //get from the database.
            //if in cache return cache
            //here check the database time and
            //decide to see whether to get the network information
            // over the net or from the database.
            // if it can be decided that the database contains all the network information
            // then go ahead and get from the database else get over the net.
            if(lastDate == null) {
                //not in the cache..
                lastDate = Calendar.getInstance().getTime();
                networkDbObjects = networkDatabase.getNetworks();
            }
            return networkDbObjects;
        }

        //get from Network.

        ArrayList arrayList = new ArrayList();

        NetworkDCOperations netdc =
            new RetryNetworkDC(networkFinderSubsetter.getNetworkDC(), 2);
        finder = netdc.a_finder();
        edu.iris.Fissures.IfNetwork.NetworkAccess[] allNets = finder.retrieve_all();
        networkIds = new NetworkId[allNets.length];
        for(int counter = 0; counter < allNets.length; counter++) {
            if (allNets[counter] != null) {
                CookieJar cookieJar = new CookieJar();
                cookieJarCache.put(allNets[counter], cookieJar);
                allNets[counter] = new RetryNetworkAccess(allNets[counter], 2);

                NetworkAttr attr = allNets[counter].get_attributes();
                networkIds[counter] = attr.get_id();
                if(networkIdSubsetter.accept(networkIds[counter], cookieJar)) {
                    //handleNetworkAttrSubsetter(allNets[counter], attr);
                    if(networkAttrSubsetter.accept(attr, cookieJar)) {
                        int dbid = networkDatabase.putNetwork(networkFinderSubsetter.getSourceName(),
                                                              networkFinderSubsetter.getDNSName(),
                                                              allNets[counter]);
                        NetworkDbObject networkDbObject = new NetworkDbObject(dbid,
                                                                              allNets[counter]);
                        arrayList.add(networkDbObject);
                    }
                } else {
                    failure.info("Fail "+attr.get_code());
                } // end of else

            } // end of if (allNets[counter] != null)

        }
        lastDate = Calendar.getInstance().getTime();
        networkDatabase.setTime(networkFinderSubsetter.getSourceName(),
                                networkFinderSubsetter.getDNSName(),
                                new MicroSecondDate(lastDate).getFissuresTime());

        networkDbObjects = new NetworkDbObject[arrayList.size()];
        networkDbObjects = (NetworkDbObject[]) arrayList.toArray(networkDbObjects);
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
        ArrayList arrayList = new ArrayList();
        try {
            CookieJar cookieJar =
                (CookieJar)cookieJarCache.get(networkDbObject.getNetworkAccess());
            Station[] stations = networkDbObject.getNetworkAccess().retrieve_stations();
            for(int subCounter = 0; subCounter < stations.length; subCounter++) {
                //      handleStationIdSubsetter(networkAccess, stations[subCounter]);
                if(stationIdSubsetter.accept(stations[subCounter].get_id(), cookieJar)) {
                    if(stationSubsetter.accept(networkDbObject.getNetworkAccess(), stations[subCounter], cookieJar)) {
                        int dbid = networkDatabase.putStation(networkDbObject, stations[subCounter]);
                        StationDbObject stationDbObject = new StationDbObject(dbid, stations[subCounter]);
                        arrayList.add(stationDbObject);
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        StationDbObject[] rtnValues = new StationDbObject[arrayList.size()];
        rtnValues = (StationDbObject[]) arrayList.toArray(rtnValues);
        networkDbObject.stationDbObjects = rtnValues;
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
        ArrayList arrayList = new ArrayList();
        NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
        Station station = stationDbObject.getStation();
        try {
            Channel[] channels = networkAccess.retrieve_for_station(station.get_id());
            for(int subCounter = 0; subCounter < channels.length; subCounter++) {
                //handleSiteIdSubsetter(networkAccess, channels[subCounter]);

                if(siteIdSubsetter.accept(channels[subCounter].my_site.get_id(), null)) {
                    if(siteSubsetter.accept(networkAccess, channels[subCounter].my_site, null)) {
                        //  int addFlag = networkDatabase.getSiteDbId(stationDbObject,
                        //                                channels[subCounter].my_site);
                        //          if(addFlag != -1) {
                        //              if(!arrayList.contains
                        //              continue;
                        //          }
                        int dbid = networkDatabase.putSite(stationDbObject,
                                                           channels[subCounter].my_site);
                        SiteDbObject siteDbObject = new SiteDbObject(dbid,
                                                                     channels[subCounter].my_site);
                        if(!containsSite(siteDbObject, arrayList)) {
                            arrayList.add(siteDbObject);
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        SiteDbObject[] rtnValues = new SiteDbObject[arrayList.size()];
        rtnValues = (SiteDbObject[]) arrayList.toArray(rtnValues);
        stationDbObject.siteDbObjects = rtnValues;
        logger.debug(" THE LENFGHT OF THE SITES IS ***************** "+rtnValues.length);
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
            logger.debug("returning from the cache");
            return siteDbObject.channelDbObjects;
        }
        ArrayList arrayList = new ArrayList();
        NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
        CookieJar cookieJar = (CookieJar)cookieJarCache.get(networkAccess);

        Site site = siteDbObject.getSite();
        try {
            Channel[] channels = networkAccess.retrieve_for_station(site.my_station.get_id());

            for(int subCounter = 0; subCounter < channels.length; subCounter++) {
                if(!isSameSite(site, channels[subCounter].my_site)) continue;
                if(channelIdSubsetter.accept(channels[subCounter].get_id(), null)) {
                    if(channelSubsetter.accept(networkAccess, channels[subCounter], null)) {
                        int dbid = networkDatabase.putChannel(siteDbObject,
                                                              channels[subCounter]);
                        ChannelDbObject channelDbObject = new ChannelDbObject(dbid,
                                                                              channels[subCounter]);
                        arrayList.add(channelDbObject);
                        handleNetworkArmProcess(networkAccess, channels[subCounter], cookieJar);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        ChannelDbObject[] values = new ChannelDbObject[arrayList.size()];
        values = (ChannelDbObject[]) arrayList.toArray(values);
        siteDbObject.channelDbObjects = values;
        logger.debug("******* The elenght of the successful channels is "+values.length);
        //  if(siteDbObject.getDbId() == 5) System.exit(0);
        return values;
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


    private Element config = null;

    private edu.sc.seis.sod.subsetter.networkArm.NetworkFinder networkFinderSubsetter = null;
    private edu.sc.seis.sod.NetworkIdSubsetter networkIdSubsetter = new NullNetworkIdSubsetter();
    private NetworkAttrSubsetter networkAttrSubsetter = new NullNetworkAttrSubsetter();
    private StationIdSubsetter stationIdSubsetter = new NullStationIdSubsetter();
    private StationSubsetter stationSubsetter = new NullStationSubsetter();
    private SiteIdSubsetter siteIdSubsetter = new NullSiteIdSubsetter();
    private SiteSubsetter siteSubsetter = new NullSiteSubsetter();
    private ChannelIdSubsetter channelIdSubsetter = new NullChannelIdSubsetter();
    private ChannelSubsetter channelSubsetter = new NullChannelSubsetter();
    private NetworkArmProcess[] networkArmProcess = {
        new NullNetworkProcess()
    };

    private NetworkStatusProcessor[] networkStatusProcess = {
        new NullNetworkStatusProcessor()
    };

    private edu.iris.Fissures.IfNetwork.NetworkFinder finder = null;
    private NetworkId[] networkIds;

    private ArrayList channelList;
    private  Channel[] successfulChannels = new Channel[0];

    private NetworkDbObject[] networkDbObjects;

    private static java.util.Date lastDate = null;

    private NetworkDatabase networkDatabase;

    private HashMap cookieJarCache = new HashMap();

    static Category logger =
        Category.getInstance(NetworkArm.class.getName());

    static Category failure = Category.getInstance(NetworkArm.class.getName()+".failure");


}// NetworkArm
