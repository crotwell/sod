package edu.sc.seis.sod;

import org.w3c.dom.*;
import org.apache.log4j.*;

import edu.sc.seis.sod.subsetter.networkArm.*;
import edu.sc.seis.fissuresUtil.chooser.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

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
		//else if(sodElement instanceof NetworkCode) handleNetworkCode(sodElement);
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
    }



    public void handleNetworkFinder(Object sodElement) {

	edu.sc.seis.sod.subsetter.networkArm.NetworkFinder networkFinder = (edu.sc.seis.sod.subsetter.networkArm.NetworkFinder)sodElement;

	NetworkDC netdc = networkFinder.getNetworkDC();
	
	ChannelChooser channelChooser = new ChannelChooser(netdc);

	channelChooser.setNetworks();

	String[] networks = channelChooser.getNetworks();
	for(int counter = 0; counter < networks.length; counter++) {


	    System.out.println("The name of the network is "+networks[counter]);

	}

    }

    

    
    static Category logger = 
        Category.getInstance(NetworkArm.class.getName());
}// NetworkArm
