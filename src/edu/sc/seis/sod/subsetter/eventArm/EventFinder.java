package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;

import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
import org.apache.log4j.*;

/**
 * EventFinder.java
 *
 *
 * Created: Tue Mar 19 12:49:48 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventFinder extends AbstractSource implements SodElement {
    public EventFinder (Element config){
	super(config);
	this.config = config;
	try {
	    processConfig();
	} catch(ConfigurationException ce) {

	    System.out.println("Configuration Exception caught in EventFinder");
	}
       
    }
    
    protected void processConfig() throws ConfigurationException{
	
	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {

	    node = childNodes.item(counter);
	    if(node instanceof Element) {

		String tagName = ((Element)node).getTagName();
		if(!tagName.equals("name") && !tagName.equals("dns")) {


		    Object object = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter.eventArm");
		    if(tagName.equals("depthRange")) depthRange = ((DepthRange)object);
		    else if(tagName.equals("eventTimeRange")) eventTimeRange = ((EventTimeRange)object);
		    else if(tagName.equals("catalog")) catalog = SodUtil.getNestedText((Element)node);
		    else if(tagName.equals("contributor")) contributor = SodUtil.getNestedText((Element)node);
		    else if(object instanceof edu.iris.Fissures.Area) area = (edu.iris.Fissures.Area)object;

		}

	    }

	}
	
    }


    private Element config = null;

    private String catalog;

    private String contributor;

    private DepthRange depthRange;

    private EventTimeRange eventTimeRange;
    
    private edu.iris.Fissures.Area area;

    static Category logger = 
        Category.getInstance(EventFinder.class.getName());

}// EventFinder
