package edu.sc.seis.sod;

import org.w3c.dom.*;
import org.apache.log4j.*;

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
		Object sodElement = SodUtil.load((Element)node);
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
    }
    
    static Category logger = 
        Category.getInstance(NetworkArm.class.getName());
}// NetworkArm
