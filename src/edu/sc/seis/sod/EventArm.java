package edu.sc.seis.sod;

import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * EventArm.java
 *
 *
 * Created: Thu Mar 14 14:09:52 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventArm {
    public EventArm (Element config) throws ConfigurationException {
	if ( ! config.getTagName().equals("eventArm")) {
	    throw new IllegalArgumentException("Configuration element must be a EventArm tag");
	}
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
		Object sodElement = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter.eventArm");
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
    }
    
    static Category logger = 
        Category.getInstance(EventArm.class.getName());
}// EventArm
