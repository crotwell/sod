package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.eventArm.*;
import edu.sc.seis.fissuresUtil.namingService.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

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
		if(sodElement instanceof edu.sc.seis.sod.subsetter.eventArm.EventFinder) eventFinderSubsetter = (edu.sc.seis.sod.subsetter.eventArm.EventFinder)sodElement;
		else if(sodElement instanceof EventAttrSubsetter) {
		    eventAttrSubsetter = (EventAttrSubsetter) sodElement;
		    
		} else if(sodElement instanceof OriginSubsetter) originSubsetter = (OriginSubsetter)sodElement;
		else if(sodElement instanceof EventArmProcess) eventArmProcess = (EventArmProcess)sodElement;
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
	processEventArm();
		
    }

    public void processEventArm() {

	EventDC eventdc = eventFinderSubsetter.getEventDC();
	finder = eventdc.a_finder();
	if(finder != null) System.out.println("Successful in getting the finder for Events");
	else System.out.println("EventFinder is null");
	String[] searchTypes = new String[0];

	EventSeqIterHolder eventSeqIterHolder = new EventSeqIterHolder();
       
	EventAccess[] eventAccess = finder.query_events(eventFinderSubsetter.getArea(),
							eventFinderSubsetter.getDepthRange().getMinDepth(),
							eventFinderSubsetter.getDepthRange().getMaxDepth(),
							eventFinderSubsetter.getEventTimeRange().getTimeRange(),
							searchTypes,
							eventFinderSubsetter.getMagnitudeRange().getMinMagnitude().value,
							eventFinderSubsetter.getMagnitudeRange().getMaxMagnitude().value,
							eventFinderSubsetter.getCatalogs(),
							eventFinderSubsetter.getContributors(),
							10,
							eventSeqIterHolder
							);
	
	for(int counter = 0; counter < eventAccess.length; counter++) {


	    EventAttr attr = eventAccess[counter].get_attributes();
	    handleEventAttrSubsetter(eventAccess[counter], attr);
	    
	}
	System.out.println("The number of events returned are "+eventAccess.length);
    }

    public void handleEventAttrSubsetter(EventAccess eventAccess, EventAttr eventAttr) {

	if(eventAttrSubsetter.accept(eventAttr, null)) {
	    try {	 
		handleOriginSubsetter(eventAccess, eventAccess.get_preferred_origin());
	    } catch(Exception e) {return;}
	}
    }

    public void handleOriginSubsetter(EventAccess eventAccess, Origin origin) {


	if(originSubsetter.accept(origin, null)) {
	    
	    handleEventArmProcess(eventAccess, origin);
	}
	
    }

    public void handleEventArmProcess(EventAccess eventAccess, Origin origin) {
	//System.out.println("passed THE TEST ************************************************************");
	eventArmProcess.process(eventAccess, origin, null);

    }

    private edu.sc.seis.sod.subsetter.eventArm.EventFinder eventFinderSubsetter;

    private edu.sc.seis.sod.EventAttrSubsetter eventAttrSubsetter = null;

    private OriginSubsetter originSubsetter;

    private EventArmProcess eventArmProcess;

    private edu.iris.Fissures.IfEvent.EventFinder finder = null;

    static Category logger = 
        Category.getInstance(EventArm.class.getName());
}// EventArm
