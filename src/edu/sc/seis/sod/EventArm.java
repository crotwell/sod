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

public class EventArm extends SodExceptionSource implements Runnable{
    /**
     * Creates a new <code>EventArm</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventArm (Element config, SodExceptionListener sodExceptionListener) throws ConfigurationException {
	if ( ! config.getTagName().equals("eventArm")) {
	    throw new IllegalArgumentException("Configuration element must be a EventArm tag");
	}
	this.config = config;
	addSodExceptionListener(sodExceptionListener);
	processConfig(config);
    }

    /**
     * Describe <code>run</code> method here.
     *
     */
    public void run() {
	try
	{
	    processEventArm();
	} catch(Exception e) {
	    
	    System.out.println("Exception caught while processing the EventArm");
	    e.printStackTrace();
	    notifyListeners(this, e);
	}
	if(eventChannelFinder == null) Start.getEventQueue().setSourceAlive(false);
	System.out.println("The number of events in the eventQueue are "
			   +Start.getEventQueue().getLength());
	
    }

    /**
     * Describe <code>processConfig</code> method here.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    protected void processConfig(Element config) 
	throws ConfigurationException {
	Start.getEventQueue().setSourceAlive(true);
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
		Object sodElement = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter.eventArm");
		if(sodElement instanceof edu.sc.seis.sod.subsetter.eventArm.EventFinder) eventFinderSubsetter = (edu.sc.seis.sod.subsetter.eventArm.EventFinder)sodElement;
		else if(sodElement instanceof edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder) eventChannelFinder = (edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder)sodElement;
		else if(sodElement instanceof EventAttrSubsetter) {
		    eventAttrSubsetter = (EventAttrSubsetter) sodElement;
		    
		} else if(sodElement instanceof OriginSubsetter) originSubsetter = (OriginSubsetter)sodElement;
		else if(sodElement instanceof EventArmProcess) eventArmProcess = (EventArmProcess)sodElement;
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)

	/*Start.getEventQueue().pop();
	Start.getEventQueue().pop();
	Start.getEventQueue().pop();
	System.out.println("event QueueLength is "+Start.getEventQueue().getLength());*/
    }

    /**
     * Describe <code>processEventArm</code> method here.
     *
     * @exception Exception if an error occurs
     */
    public void processEventArm() throws Exception{

	if(eventChannelFinder != null) {

	    Thread thread = new Thread(eventChannelFinder);
	    thread.start();
	} else {

	    System.out.println("EventChannelFinder is NULL");
	}
	if(eventFinderSubsetter == null) return;
	EventDC eventdc = eventFinderSubsetter.getEventDC();
	finder = eventdc.a_finder();
	String[] searchTypes = new String[3];
	searchTypes[0] = "MB";
	searchTypes[1] = "ML";
	searchTypes[2] = "MS";

	EventSeqIterHolder eventSeqIterHolder = new EventSeqIterHolder();
       
	Quantity minDepth;
	Quantity maxDepth;
	float minMagnitude;
	float maxMagnitude;
	if(eventFinderSubsetter.getDepthRange() != null) {

	    minDepth = eventFinderSubsetter.getDepthRange().getMinDepth();
	    maxDepth = eventFinderSubsetter.getDepthRange().getMaxDepth();

	} else {
	    
	    minDepth = new QuantityImpl(-90000.0, UnitImpl.KILOMETER);
	    maxDepth = new QuantityImpl(90000.0, UnitImpl.KILOMETER);
	}
	
	if(eventFinderSubsetter.getMagnitudeRange() == null) {

	    minMagnitude = -99.0f;
	    maxMagnitude = 99.0f;

	} else {
	    
	    minMagnitude = eventFinderSubsetter.getMagnitudeRange().getMinMagnitude().value;
	    maxMagnitude = eventFinderSubsetter.getMagnitudeRange().getMaxMagnitude().value;
	}
	    
	System.out.println("getting events from "+eventFinderSubsetter.getEventTimeRange().getTimeRange().start_time.date_time+" to "+eventFinderSubsetter.getEventTimeRange().getTimeRange().end_time.date_time);
	for (int i=0; i<searchTypes.length; i++) {
	System.out.println("magnitudes "+searchTypes[i]);
	} // end of for (int i=0; i<searchTypes.length; i++)
	System.out.println("mag "+minMagnitude+" "+maxMagnitude);

	EventAccess[] eventAccess = finder.query_events(eventFinderSubsetter.getArea(),
							minDepth,
							maxDepth,
							eventFinderSubsetter.getEventTimeRange().getTimeRange(),
							searchTypes,
							minMagnitude,
							maxMagnitude,
							eventFinderSubsetter.getCatalogs(),
							eventFinderSubsetter.getContributors(),
							10,
							eventSeqIterHolder
							);
	
	System.out.println("The number of events returned are "+eventAccess.length);
	for(int counter = 0; counter < eventAccess.length; counter++) {


	    EventAttr attr = eventAccess[counter].get_attributes();
	    handleEventAttrSubsetter(eventAccess[counter], attr);
	    
	}
	System.out.println("The number of events returned are "+eventAccess.length);
    }

    /**
     * Describe <code>handleEventAttrSubsetter</code> method here.
     *
     * @param eventAccess an <code>EventAccess</code> value
     * @param eventAttr an <code>EventAttr</code> value
     * @exception Exception if an error occurs
     */
    public void handleEventAttrSubsetter(EventAccess eventAccess, EventAttr eventAttr) throws Exception {

	if(eventAttrSubsetter.accept(eventAttr, null)) {
	    try {	 
		handleOriginSubsetter(eventAccess, eventAccess.get_preferred_origin());
	    } catch(Exception e) {return;}
	}
    }

    /**
     * Describe <code>handleOriginSubsetter</code> method here.
     *
     * @param eventAccess an <code>EventAccess</code> value
     * @param origin an <code>Origin</code> value
     * @exception Exception if an error occurs
     */
    public void handleOriginSubsetter(EventAccess eventAccess, Origin origin) throws Exception{


	if(originSubsetter.accept(eventAccess, origin, null)) {
	    
	    handleEventArmProcess(eventAccess, origin);
	}
	
    }

    /**
     * Describe <code>handleEventArmProcess</code> method here.
     *
     * @param eventAccess an <code>EventAccess</code> value
     * @param origin an <code>Origin</code> value
     * @exception Exception if an error occurs
     */
    public void handleEventArmProcess(EventAccess eventAccess, Origin origin) throws Exception{
	Start.getEventQueue().push(eventAccess);
	eventArmProcess.process(eventAccess, null);

    }

    private edu.sc.seis.sod.subsetter.eventArm.EventFinder eventFinderSubsetter;

    private edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder eventChannelFinder = null;

    private edu.sc.seis.sod.EventAttrSubsetter eventAttrSubsetter = new NullEventAttrSubsetter();

    private OriginSubsetter originSubsetter = new NullOriginSubsetter();

    private EventArmProcess eventArmProcess = new NullEventProcess();

    private edu.iris.Fissures.IfEvent.EventFinder finder = null;

    private Element config = null;

    static Category logger = 
        Category.getInstance(EventArm.class.getName());
}// EventArm
