package edu.sc.seis.sod;

import org.w3c.dom.*;

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
    public EventArm (Element config){
	if ( ! config.getTagname().equals("EventArm")) {
	    throw new IllegalArgument("Configuration element must be a EventArm tag");
	}
	processConfig(config);
    }

    protected void processConfig(Element config) {
	NodeList children = config.getChildNodes();
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	} // end of for (int i=0; i<children.getSize(); i++)
	
    }
    
}// EventArm
