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

/**
 * NetworkArm.java
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
	//	processConfig(config);
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
	    logger.debug(node.getNodeName());
	    if (node instanceof Element) {
		if (((Element)node).getTagName().equals("description")) {
		    // skip description element
		    continue;
		}

		Object sodElement = SodUtil.load((Element)node,"edu.sc.seis.sod.subsetter.networkArm");
		if(sodElement instanceof edu.sc.seis.sod.subsetter.networkArm.NetworkFinder)  networkFinderSubsetter = (edu.sc.seis.sod.subsetter.networkArm.NetworkFinder)sodElement;
		else if(sodElement instanceof NetworkIdSubsetter) { 
		    networkIdSubsetter = (edu.sc.seis.sod.NetworkIdSubsetter)sodElement;
		} else if(sodElement instanceof NetworkAttrSubsetter) {

		    networkAttrSubsetter = (NetworkAttrSubsetter)sodElement;
		} else if(sodElement instanceof StationIdSubsetter)stationIdSubsetter = (StationIdSubsetter)sodElement;
		else if(sodElement instanceof StationSubsetter) stationSubsetter = (StationSubsetter)sodElement;
		else if(sodElement instanceof SiteIdSubsetter) siteIdSubsetter = (SiteIdSubsetter)sodElement;
		else if(sodElement instanceof SiteSubsetter) siteSubsetter = (SiteSubsetter)sodElement;
		else if(sodElement instanceof ChannelIdSubsetter) channelIdSubsetter = (ChannelIdSubsetter)sodElement;
		else if(sodElement instanceof ChannelSubsetter) channelSubsetter = (ChannelSubsetter)sodElement;
		else if(sodElement instanceof NetworkArmProcess) networkArmProcess = (NetworkArmProcess)sodElement;

	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)

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
	networkIds = new NetworkId[allNets.length];
	for(int counter = 0; counter < allNets.length; counter++) {
	    if (allNets[counter] != null) {
		 
	    
	    NetworkAttr attr = allNets[counter].get_attributes();
	    networkIds[counter] = attr.get_id();
	    if(networkIdSubsetter.accept(networkIds[counter], null)) {
		handleNetworkAttrSubsetter(allNets[counter], attr);
	
	    } else {
		failure.info("Fail "+attr.get_code());
	    } // end of else

	    } // end of if (allNets[counter] != null)
	    
	}
	successfulChannels = new Channel[channelList.size()];
	successfulChannels = (Channel[]) channelList.toArray(successfulChannels);
    }

    /**
     * Describe <code>handleNetworkAttrSubsetter</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param networkAttr a <code>NetworkAttr</code> value
     * @exception Exception if an error occurs
     */
    public void handleNetworkAttrSubsetter(NetworkAccess networkAccess, NetworkAttr networkAttr) throws Exception{

	try {
	    //System.out.println("The stationIdSubsetter is not null");
	    if(networkAttrSubsetter.accept(networkAttr, null)) { 
		Station[] stations = networkAccess.retrieve_stations();
		for(int subCounter = 0; subCounter < stations.length; subCounter++) {
		    handleStationIdSubsetter(networkAccess, stations[subCounter]);
		}
	    } else {
		failure.info("Fail NetworkAttr"+networkAttr.get_code());
	    }
	} catch (org.omg.CORBA.UNKNOWN e) {
	    logger.warn("Caught exception, network is: "+NetworkIdUtil.toString(networkAttr.get_id()), e);
	    throw e;
	} // end of try-catch
	    
    }

    /**
     * Describe <code>handleStationIdSubsetter</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @exception Exception if an error occurs
     */
    public void handleStationIdSubsetter(NetworkAccess networkAccess, Station  station) throws Exception{
	try {
	     
	if(stationIdSubsetter.accept(station.get_id(), null)) {
	    handleStationSubsetter(networkAccess, station); 
	} else {
		failure.info("Fail StationId"+station.get_code());
	    }
	} catch (org.omg.CORBA.UNKNOWN e) {
	    logger.warn("Caught exception, station is: "+StationIdUtil.toString(station.get_id()), e);
	    throw e;
	} // end of try-catch
	
    }

    /**
     * Describe <code>handleStationSubsetter</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @exception Exception if an error occurs
     */
    public void handleStationSubsetter(NetworkAccess networkAccess, Station station) throws Exception{
	
	if(stationSubsetter.accept(networkAccess, station, null)) {
	    Channel[] channels = networkAccess.retrieve_for_station(station.get_id());
	    for(int subCounter = 0; subCounter < channels.length; subCounter++) {
		handleSiteIdSubsetter(networkAccess, channels[subCounter]);
	

	    }
	} else {
		failure.info("Fail Station"+station.get_code());
	    }
    }
				       
    /**
     * Describe <code>handleSiteIdSubsetter</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleSiteIdSubsetter(NetworkAccess networkAccess, Channel channel) throws Exception{
	
	if(siteIdSubsetter.accept(channel.my_site.get_id(), null)) {
	    handleSiteSubsetter(networkAccess, channel);
	} else {
		failure.info("Fail SiteId "+SiteIdUtil.toString(channel.my_site.get_id()));
	    }
    }

    /**
     * Describe <code>handleSiteSubsetter</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleSiteSubsetter(NetworkAccess networkAccess, Channel channel) throws Exception{

	if(siteSubsetter.accept(networkAccess, channel.my_site, null)) {
	    handleChannelIdSubsetter(networkAccess, channel);
	} else {
		failure.info("Fail Site "+SiteIdUtil.toString(channel.my_site.get_id()));
	    }
    }

    /**
     * Describe <code>handleChannelIdSubsetter</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleChannelIdSubsetter(NetworkAccess networkAccess, Channel channel) throws Exception{

	if(channelIdSubsetter.accept(channel.get_id(), null)) {
	    handleChannelSubsetter(networkAccess, channel);
	} else {
		failure.info("Fail ChannelId"+ChannelIdUtil.toStringNoDates(channel.get_id()));
	    }
       
    }
    
    /**
     * Describe <code>handleChannelSubsetter</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleChannelSubsetter(NetworkAccess networkAccess, Channel channel) throws Exception{

	if(channelSubsetter.accept(networkAccess, channel, null)) {
	    handleNetworkArmProcess(networkAccess, channel);
	} else {
		failure.info("Fail Channel "+ChannelIdUtil.toStringNoDates(channel.get_id()));
	    }
    }

    /**
     * Describe <code>handleNetworkArmProcess</code> method here.
     *
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @exception Exception if an error occurs
     */
    public void handleNetworkArmProcess(NetworkAccess networkAccess, Channel channel) throws Exception{
	networkArmProcess.process(networkAccess, channel, null);

    }

    public Channel getChannel(int dbid) {
	return networkDatabase.getChannel(dbid);
    }

    public NetworkAccess getNetworkAccess(int dbid) {
	return networkDatabase.getNetworkAccess(dbid);
    }

    public int getSiteDbId(int channelid) {
	return networkDatabase.getSiteDbId(channelid);
    }

    public int getStationDbId(int siteid) {
	return networkDatabase.getStationDbId(siteid);	
    }

    public int getNetworkDbId(int stationid) {
	return networkDatabase.getNetworkDbId(stationid);
    }

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
	
	NetworkDC netdc = networkFinderSubsetter.getNetworkDC();
	finder = netdc.a_finder();
	edu.iris.Fissures.IfNetwork.NetworkAccess[] allNets = finder.retrieve_all();
	networkIds = new NetworkId[allNets.length];
	for(int counter = 0; counter < allNets.length; counter++) {
	    if (allNets[counter] != null) {
		NetworkAttr attr = allNets[counter].get_attributes();
		networkIds[counter] = attr.get_id();
		if(networkIdSubsetter.accept(networkIds[counter], null)) {
		    //handleNetworkAttrSubsetter(allNets[counter], attr);
		    if(networkAttrSubsetter.accept(attr, null)) { 
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

    public StationDbObject[] getSuccessfulStations(NetworkDbObject networkDbObject) {
	if(networkDbObject.stationDbObjects != null) {
	    System.out.println("returning from the cache");
	    return networkDbObject.stationDbObjects;
	} 
	ArrayList arrayList = new ArrayList();
	try {
	    Station[] stations = networkDbObject.getNetworkAccess().retrieve_stations();
	    for(int subCounter = 0; subCounter < stations.length; subCounter++) {
		//	    handleStationIdSubsetter(networkAccess, stations[subCounter]);
	    	if(stationIdSubsetter.accept(stations[subCounter].get_id(), null)) {
		    if(stationSubsetter.accept(networkDbObject.getNetworkAccess(), stations[subCounter], null)) {
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

    public SiteDbObject[] getSuccessfulSites(NetworkDbObject networkDbObject, StationDbObject stationDbObject) {
	if(stationDbObject.siteDbObjects != null) {
	    System.out.println("returning from the cache");
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
		// 	int addFlag = networkDatabase.getSiteDbId(stationDbObject,
// 								  channels[subCounter].my_site);
// 			if(addFlag != -1) {
// 			    if(!arrayList.contains
// 			    continue;
// 			}
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
	System.out.println(" THE LENFGHT OF THE SITES IS ***************** "+rtnValues.length);
	return rtnValues;

    }

    private boolean containsSite(SiteDbObject siteDbObject, ArrayList arrayList) {
	for(int counter = 0; counter < arrayList.size(); counter++) {
	    SiteDbObject tempObject = (SiteDbObject) arrayList.get(counter);
	    if(tempObject.getDbId() == siteDbObject.getDbId()) return true;
	}
	return false;
    }
    


    public ChannelDbObject[] getSuccessfulChannels(NetworkDbObject networkDbObject, SiteDbObject siteDbObject) {
	if(siteDbObject.channelDbObjects != null) {
	    System.out.println("returning from the cache");
	    return siteDbObject.channelDbObjects;
	}
	ArrayList arrayList = new ArrayList();
	NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
	Site site = siteDbObject.getSite();
	try {
	    Channel[] channels = networkAccess.retrieve_for_station(site.my_station.get_id());
	    for(int subCounter = 0; subCounter < channels.length; subCounter++) {
		if(channelIdSubsetter.accept(channels[subCounter].get_id(), null)) {
		    if(channelSubsetter.accept(networkAccess, channels[subCounter], null)) {
			int dbid = networkDatabase.putChannel(siteDbObject,
							      channels[subCounter]);
			ChannelDbObject channelDbObject = new ChannelDbObject(dbid,
									      channels[subCounter]);
			arrayList.add(channelDbObject);
			handleNetworkArmProcess(networkAccess, channels[subCounter]);
		    }
		}
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	}
	ChannelDbObject[] values = new ChannelDbObject[arrayList.size()];
	values = (ChannelDbObject[]) arrayList.toArray(values);
	siteDbObject.channelDbObjects = values;
	System.out.println("******* The elenght of the successful channels is "+values.length);
	return values;
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
    private NetworkArmProcess networkArmProcess = new NullNetworkProcess();

    private edu.iris.Fissures.IfNetwork.NetworkFinder finder = null;
    private NetworkId[] networkIds; 

    private ArrayList channelList;
    private  Channel[] successfulChannels = new Channel[0];

    private NetworkDbObject[] networkDbObjects;
    
    private static java.util.Date lastDate = null;

    private NetworkDatabase networkDatabase;

    static Category logger = 
        Category.getInstance(NetworkArm.class.getName());
    
    static Category failure = Category.getInstance(NetworkArm.class.getName()+".failure");
    
    
    }// NetworkArm
