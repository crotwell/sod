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
	/*try {
	    processNetworkArm();

	} catch(Exception e) {

	    System.out.println("Exception caught while processing Network Arm");
	    }*/
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
	channelList.add(channel);
	networkDatabase.put(networkFinderSubsetter.getSourceName(),
			    networkFinderSubsetter.getDNSName(),
			    channel, networkAccess);
	networkArmProcess.process(networkAccess, channel, null);

    }

    /**
     * Describe <code>getSuccessfulChannels</code> method here.
     *
     * @return a <code>Channel[]</code> value
     */
    public Channel[] getSuccessfulChannels() throws Exception{

	    RefreshInterval refreshInterval = networkFinderSubsetter.getRefreshInterval();
	 //    if(lastDate != null && refreshInterval != null) {
		
// 		//RefreshInterval refreshInterval = networkFinderSubsetter.getRefreshInterval();
// 		Date currentDate = Calendar.getInstance().getTime();
// 		MicroSecondDate lastTime = new MicroSecondDate(lastDate);
// 		MicroSecondDate currentTime = new MicroSecondDate(currentDate);
// 		TimeInterval timeInterval = currentTime.difference(lastTime);
// 		timeInterval = (TimeInterval)timeInterval.convertTo(UnitImpl.MINUTE);
// 		int minutes = (int)timeInterval.value;
// 		//System.out.println("The number of minutes since the network Arm is Processed -------------->"+minutes);
// 		if(minutes >= refreshInterval.getValue()) {
// 		    processNetworkArm();
// 		    lastDate = Calendar.getInstance().getTime();
// 		}
// 	    } else {
// 		edu.iris.Fissures.Time databaseTime = networkDatabase.getTime(eventFinderSubsetter.getSourceName(),
// 									      eventFinderSubsetter.getDNSName());
		
// 		if(databaseTime != null) {
// 		    lastDate = new MicroSecondDate(databaseTime);
// 		    //successfulChannels = networkDatabase.getChannels();
// 		} else {						      
// 		    processNetworkArm();
// 		    lastDate = Calendar.getInstance().getTime();
// 		    networkDatabase.setTime(networkFinderSubsetter.getSourceName(),
// 					    networkFinderSubsetter.getDNSName(),
// 					    new MicroSecondDate(lastDate).getFissuresTime());
// 		}
		 
// 	    }
	    //System.out.println("successfulChannels length is "+successfulChannels.length);

	    edu.iris.Fissures.Time databaseTime = networkDatabase.getTime(networkFinderSubsetter.getSourceName(),
									  networkFinderSubsetter.getDNSName());
	    
	    if(databaseTime != null) {
		
		if(refreshInterval == null) {
		    successfulChannels = networkDatabase.getChannels();
		  
		    return successfulChannels;
		}

		Date currentDate = Calendar.getInstance().getTime();
		MicroSecondDate lastTime = new MicroSecondDate(databaseTime);
		MicroSecondDate currentTime = new MicroSecondDate(databaseTime);
		TimeInterval timeInterval = currentTime.difference(lastTime);
		timeInterval = (TimeInterval)timeInterval.convertTo(UnitImpl.MINUTE);
		int minutes = (int)timeInterval.value;
		//System.out.println("The number of minutes since the network Arm is Processed -------------->"+minutes);
		if(minutes >= refreshInterval.getValue()) {
		    //here may have to delete all the channels from the networkDatabase.
		    processNetworkArm();
		    lastDate = currentDate;
		    networkDatabase.setTime(networkFinderSubsetter.getSourceName(),
					    networkFinderSubsetter.getDNSName(),
					    new MicroSecondDate(lastDate).getFissuresTime());
		}  else if(lastDate == null) {
		      successfulChannels = networkDatabase.getChannels();
		      lastDate = new MicroSecondDate(databaseTime);
		      return successfulChannels;
		}

		
	    } else {						      
		processNetworkArm();
		lastDate = Calendar.getInstance().getTime();
		networkDatabase.setTime(networkFinderSubsetter.getSourceName(),
					networkFinderSubsetter.getDNSName(),
					new MicroSecondDate(lastDate).getFissuresTime());
	    } 
	    return successfulChannels;
	    
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
    
    private static java.util.Date lastDate = null;

    private NetworkDatabase networkDatabase;

    static Category logger = 
        Category.getInstance(NetworkArm.class.getName());
    
    static Category failure = Category.getInstance(NetworkArm.class.getName()+".failure");
    
    
    }// NetworkArm
