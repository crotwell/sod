package edu.sc.seis.sod;

import org.w3c.dom.*;
import org.apache.log4j.*;

import edu.sc.seis.sod.subsetter.networkArm.*;
import edu.sc.seis.fissuresUtil.chooser.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

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
    public NetworkArm (Element config) throws ConfigurationException {
	if ( ! config.getTagName().equals("networkArm")) {
	    throw new IllegalArgumentException("Configuration element must be a NetworkArm tag");
	}
	System.out.println("In Network Arm");
	processConfig(config);
    }

    protected void processConfig(Element config) 
	throws ConfigurationException {

	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    logger.debug(node.getNodeName());
	    if (node instanceof Element) {
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
	processNetworkArm();	
    }

    /*
     *This function starts the processing of the network Arm.
     *It gets all the NetworkAccesses and network ids from them
     *and checks if the networkId is accepted by the networkIdSubsetter.
     **/
    
    public void processNetworkArm() {


	NetworkDC netdc = networkFinderSubsetter.getNetworkDC();
        finder = netdc.a_finder();
	edu.iris.Fissures.IfNetwork.NetworkAccess[] allNets = finder.retrieve_all();
	networkIds = new NetworkId[allNets.length];
	for(int counter = 0; counter < allNets.length; counter++) {
	    NetworkAttr attr = allNets[counter].get_attributes();
	    networkIds[counter] = attr.get_id();
	    if(networkIdSubsetter.accept(networkIds[counter], null)) {
		handleNetworkAttrSubsetter(allNets[counter], attr);
	    }
	}
    }

    public void handleNetworkAttrSubsetter(NetworkAccess networkAccess, NetworkAttr networkAttr) {

	System.out.println("The stationIdSubsetter is not null");
	if(networkAttrSubsetter.accept(networkAttr, null)) { 
	    Station[] stations = networkAccess.retrieve_stations();
	    for(int subCounter = 0; subCounter < stations.length; subCounter++) {
		handleStationIdSubsetter(networkAccess, stations[subCounter]);
	    }
	}

    }

    public void handleStationIdSubsetter(NetworkAccess networkAccess, Station  station) {
	
	if(stationIdSubsetter.accept(station.get_id(), null)) {
	    handleStationSubsetter(networkAccess, station); 
	}
    }

    public void handleStationSubsetter(NetworkAccess networkAccess, Station station) {
	
	if(stationSubsetter.accept(station, null)) {
	    Channel[] channels = networkAccess.retrieve_for_station(station.get_id());
	    for(int subCounter = 0; subCounter < channels.length; subCounter++) {
		handleSiteIdSubsetter(networkAccess, channels[subCounter]);
	    }
	}
    }
				       
    public void handleSiteIdSubsetter(NetworkAccess networkAccess, Channel channel) {
	
	if(siteIdSubsetter.accept(channel.my_site.get_id(), null)) {
	    handleSiteSubsetter(networkAccess, channel);
	}
    }

    public void handleSiteSubsetter(NetworkAccess networkAccess, Channel channel) {

	if(siteSubsetter.accept(channel.my_site, null)) {
	    handleChannelIdSubsetter(networkAccess, channel);
	}
    }

    public void handleChannelIdSubsetter(NetworkAccess networkAccess, Channel channel) {

	if(channelIdSubsetter.accept(channel.get_id(), null)) {
	    handleChannelSubsetter(networkAccess, channel);
	}
       
    }
    
    public void handleChannelSubsetter(NetworkAccess networkAccess, Channel channel) {

	if(channelSubsetter.accept(channel, null)) {
	    handleNetworkArmProcess(networkAccess, channel);
	}
    }

    public void handleNetworkArmProcess(NetworkAccess networkAccess, Channel channel) {

	networkArmProcess.process(networkAccess, channel, null);

    }

    private edu.sc.seis.sod.subsetter.networkArm.NetworkFinder networkFinderSubsetter = null;
    private edu.sc.seis.sod.NetworkIdSubsetter networkIdSubsetter = new NullNetworkIdSubsetter(); 
    private NetworkAttrSubsetter networkAttrSubsetter = new NullNetworkAttrSubsetter();
    private StationIdSubsetter stationIdSubsetter = new NullStationIdSubsetter();
    private StationSubsetter stationSubsetter = new NullStationSubsetter();
    private SiteIdSubsetter siteIdSubsetter = new NullSiteIdSubsetter();
    private SiteSubsetter siteSubsetter = new NullSiteSubsetter();
    private ChannelIdSubsetter channelIdSubsetter = new NullChannelIdSubsetter();
    private ChannelSubsetter channelSubsetter = new NullChannelSubsetter();
    private NetworkArmProcess networkArmProcess = null;

    private edu.iris.Fissures.IfNetwork.NetworkFinder finder = null;
    private NetworkId[] networkIds; 
    
    static Category logger = 
        Category.getInstance(NetworkArm.class.getName());
    }// NetworkArm
