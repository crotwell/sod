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
		if(sodElement instanceof edu.sc.seis.sod.subsetter.networkArm.NetworkFinder) handleNetworkFinder(sodElement);
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

	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
	processNetworkArm();	
    }

    public void processNetworkArm() {
	ArrayList arrayList = new ArrayList();

	if(networkIdSubsetter == null){handleNetworkAttrSubsetter(new NetworkAttr[0]);return;}
	for(int counter = 0; counter < networkIds.length; counter++) {
		if(networkIdSubsetter.accept(networkIds[counter], null)) {
		     try {
			NetworkAccess[] networkAccess = finder.retrieve_by_code(networkIds[counter].network_code);
			NetworkAttr attr = networkAccess[0].get_attributes();
			arrayList.add(attr);	
		     }catch(NetworkNotFound nnfe) {
			System.out.println("Caught Exception Network Not Found");
		     }
		}
	}
	NetworkAttr[] networkAttributes = new NetworkAttr[arrayList.size()];
	networkAttributes = (NetworkAttr[]) arrayList.toArray(networkAttributes);
	handleNetworkAttrSubsetter(networkAttributes);	
    }

    public void handleNetworkAttrSubsetter(NetworkAttr[] networkAttributes) {

	System.out.println("The length of network Attributes is "+networkAttributes.length);
	if(networkAttrSubsetter == null)  {handleStationIdSubsetter(new Station[0]);return;}
	System.out.println("The stationIdSubsetter is not null");
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < networkAttributes.length; counter++) {
		if(networkAttrSubsetter.accept(networkAttributes[counter], null)) { 
			try {	
				NetworkAccess[] networkAccess = finder.retrieve_by_code(networkAttributes[counter].get_code());
				Station[] stations = networkAccess[0].retrieve_stations();
				for(int subCounter = 0; subCounter < stations.length; subCounter++) {
					arrayList.add(stations[subCounter]);
				}
			} catch(NetworkNotFound nnfe) {

				System.out.println("caught Network Not Found Exception");	
			}

		}
	}
	Station[] stations = new Station[arrayList.size()];
	stations = (Station[]) arrayList.toArray(stations);
	handleStationIdSubsetter(stations);
    }

    public void handleStationIdSubsetter(Station[] paramStations) {
	System.out.println("The lenght of stationIds is "+paramStations.length);
	if(stationIdSubsetter == null){ handleStationSubsetter(paramStations); return;}
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < paramStations.length; counter++) {
		if(stationIdSubsetter.accept(paramStations[counter].get_id(), null)) {
			arrayList.add(paramStations[counter]);
		}
	}
	Station[] stations = new Station[arrayList.size()];
	stations = (Station[])arrayList.toArray(stations);
	handleStationSubsetter(stations);
   }

   public void handleStationSubsetter(Station[] stations) {
       System.out.println("The length  of stations is "+stations.length);
	//getStations
	if(stationSubsetter == null) {handleSiteIdSubsetter(new Channel[0]);return;}
	ArrayList arrayList = new ArrayList();
	String networkCode = "";
	for(int counter = 0; counter < stations.length; counter++) {
	    try {
		if(stationSubsetter.accept(stations[counter], null)) {
		    String tempCode = stations[counter].my_network.get_code();
		    if(!tempCode.equals(networkCode)) {
			NetworkAccess[] networkAccess = finder.retrieve_by_code(stations[counter].my_network.get_code());
			Channel[] channels = networkAccess[0].retrieve_for_station(stations[counter].get_id());
			for(int subCounter = 0; subCounter < channels.length; subCounter++) {
			    arrayList.add(channels[subCounter]);
			}
			networkCode = tempCode;
		    }
		}
	    } catch(NetworkNotFound nnfe) {

		System.out.println("Caught the exception network not found");

	    }
	}
	Channel[] channels = new Channel[arrayList.size()];
	channels = (Channel[]) arrayList.toArray(channels);
	handleSiteIdSubsetter(channels);
   }

   public void handleSiteIdSubsetter(Channel[] paramChannels) {
	//getSiteIds
       System.out.println("The lenght of the channels is "+paramChannels.length);
       if(siteIdSubsetter == null) handleSiteSubsetter(paramChannels);
       ArrayList arrayList = new ArrayList();
       for(int counter = 0; counter < paramChannels.length; counter++) {


	   if(siteIdSubsetter.accept(paramChannels[counter].my_site.get_id(), null)) {
	       
	       arrayList.add(paramChannels[counter]);
	       
	   }
	       
       }
       Channel[] channels = new Channel[arrayList.size()];
       channels = (Channel[]) arrayList.toArray(channels);
       handleSiteSubsetter(channels);
					   

   }

   public void handleSiteSubsetter(Channel[] paramChannels) {

       System.out.println("The lenght of the channels after siteID is "+paramChannels.length);
       if(siteSubsetter == null) handleChannelIdSubsetter(paramChannels);
       ArrayList arrayList = new ArrayList();
       for(int counter = 0; counter < paramChannels.length; counter++) {

	   if(siteSubsetter.accept(paramChannels[counter].my_site, null)) {
	       
	       arrayList.add(paramChannels[counter]);
	       
	   }
	       
       }
       Channel[] channels = new Channel[arrayList.size()];
       channels = (Channel[]) arrayList.toArray(channels);
       handleChannelIdSubsetter(channels);

  }

  public void handleChannelIdSubsetter(Channel[] paramChannels) {
      
       System.out.println("The lenght of the channels after site is "+paramChannels.length);
       if(channelIdSubsetter == null) handleChannelSubsetter(paramChannels);
       ArrayList arrayList = new ArrayList();
       for(int counter = 0; counter < paramChannels.length; counter++) {

	   if(channelIdSubsetter.accept(paramChannels[counter].get_id(), null)) {
	       
	       arrayList.add(paramChannels[counter]);
	       
	   }
	       
       }
       Channel[] channels = new Channel[arrayList.size()];
       channels = (Channel[]) arrayList.toArray(channels);
       handleChannelSubsetter(channels);



  }

  public void handleChannelSubsetter(Channel[] paramChannels) {
       System.out.println("The lenght of the channels after ChannelID is "+paramChannels.length);
       ArrayList arrayList = new ArrayList();
       if(channelSubsetter == null) {
	   
	   for(int counter = 0; counter < paramChannels.length; counter++) arrayList.add(paramChannels);
	   
       } else {
	
	   for(int counter = 0; counter < paramChannels.length; counter++) {
	       
	       if(channelSubsetter.accept(paramChannels[counter], null)) {
		   
		   arrayList.add(paramChannels[counter]);
		   
	       }
	       
	   }
       }
       Channel[] channels = new Channel[arrayList.size()];
       channels = (Channel[]) arrayList.toArray(channels);
       printChannels(channels);


  }

    public void printChannels(Channel[] channels) {

	System.out.println("The length of the Channels after entire processing "+channels.length);

	System.out.println("**********************************************");
	System.out.println("	      SUCCESSFUL CHANNELS   ");
	System.out.println("**********************************************");
	for(int counter = 0; counter < channels.length; counter++) {

	    System.out.println(ChannelIdUtil.toString(channels[counter].get_id()));

	}

    }

    public void handleNetworkFinder(Object sodElement) {

	edu.sc.seis.sod.subsetter.networkArm.NetworkFinder networkFinder = (edu.sc.seis.sod.subsetter.networkArm.NetworkFinder)sodElement;

	NetworkDC netdc = networkFinder.getNetworkDC();
        finder = netdc.a_finder();
	edu.iris.Fissures.IfNetwork.NetworkAccess[] allNets = finder.retrieve_all();
	networkIds = new NetworkId[allNets.length];
	for(int counter = 0; counter < allNets.length; counter++) {
		NetworkAttr attr = allNets[counter].get_attributes();
		networkIds[counter] = attr.get_id();
	}
    }

    private edu.sc.seis.sod.NetworkIdSubsetter networkIdSubsetter = null; 
    private NetworkAttrSubsetter networkAttrSubsetter = null;
    private StationIdSubsetter stationIdSubsetter = null;
    private StationSubsetter stationSubsetter = null;
    private SiteIdSubsetter siteIdSubsetter = null;
    private SiteSubsetter siteSubsetter = null;
    private ChannelIdSubsetter channelIdSubsetter = null;
    private ChannelSubsetter channelSubsetter = null;

    private edu.iris.Fissures.IfNetwork.NetworkFinder finder = null;
    private NetworkId[] networkIds; 
    
    static Category logger = 
        Category.getInstance(NetworkArm.class.getName());
}// NetworkArm
