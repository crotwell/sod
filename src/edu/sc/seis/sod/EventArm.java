package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;
import java.util.*;

import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder;
import edu.sc.seis.sod.subsetter.eventArm.EventFinder;
import edu.sc.seis.sod.subsetter.eventArm.EventTimeRange;
import edu.sc.seis.sod.subsetter.eventArm.NullEventAttrSubsetter;
import edu.sc.seis.sod.subsetter.eventArm.NullOriginSubsetter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class handles the subsetting of the Events based on the subsetters specified
 * in the configuration file (xml file).
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
    public EventArm (Element config, SodExceptionListener sodExceptionListener, Properties props) throws ConfigurationException {
        if ( ! config.getTagName().equals("eventArm")) {
            throw new IllegalArgumentException("Configuration element must be a EventArm tag");
        }
        this.props = props;
        addSodExceptionListener(sodExceptionListener);
        processConfig(config);
    }
    
    public void run() {
        try {
            getEvents();
        } catch(Exception e) {
            logger.error("Exception caught while processing the EventArm", e);
            //notify Exception listeners in case of an exception
            notifyListeners(this, e);
        }
        logger.debug("Before setting the source Alive to be false");
        // if eventChannelFinder is false ... i.e., if no eventChannel is configured
        // then the eventArm can signal end of processing
        // by setting the sourceAlive attribute of the EventQueue to be false.
        
        if(eventChannelFinder == null) Start.getEventQueue().setSourceAlive(false);
        logger.debug("After setting the source Alive to be false");
        logger.debug("IN EVENT ARM **** The number of events in the eventQueue are "
                         +Start.getEventQueue().getLength());
    }
    
    /**
     * This method processes the eventArm configuration
     *
     */
    private void processConfig(Element config)
        throws ConfigurationException {
        Start.getEventQueue().setSourceAlive(true);
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter.eventArm");
                if(sodElement instanceof edu.sc.seis.sod.subsetter.eventArm.EventFinder) {
                    eventFinderSubsetter = (edu.sc.seis.sod.subsetter.eventArm.EventFinder)sodElement;
                } else if(sodElement instanceof edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder) {
                    eventChannelFinder = (edu.sc.seis.sod.subsetter.eventArm.EventChannelFinder)sodElement;
                } else if(sodElement instanceof EventAttrSubsetter) {
                    eventAttrSubsetter = (EventAttrSubsetter) sodElement;
                } else if(sodElement instanceof OriginSubsetter) {
                    originSubsetter = (OriginSubsetter)sodElement;
                } else if(sodElement instanceof EventArmProcess) {
                    processors.add(sodElement);
                }else if(sodElement instanceof EventStatus) {
                    statusMonitors.add(sodElement);
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }
    
    private void getEvents() throws Exception{
        if(eventChannelFinder != null) {
            eventChannelFinder.setEventArm(this);
            Thread thread = new Thread(eventChannelFinder);
            thread.setName("EventChannelFinder");
            thread.start();
        } else {
            logger.info("EventChannelFinder is not being used.");
        }
        if(eventFinderSubsetter == null) return;
        logger.info("Event Finder query is being used");
        EventDC eventdc = eventFinderSubsetter.getEventDC();
        edu.iris.Fissures.IfEvent.EventFinder finder = eventdc.a_finder();
        String[] searchTypes;
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
            searchTypes = new String[1];
            searchTypes[0] = "%";
        } else {
            minMagnitude = eventFinderSubsetter.getMagnitudeRange().getMinMagnitude().value;
            maxMagnitude = eventFinderSubsetter.getMagnitudeRange().getMaxMagnitude().value;
            searchTypes = eventFinderSubsetter.getMagnitudeRange().getSearchTypes();
        }
        logger.debug("getting events from "+eventFinderSubsetter.getEventTimeRange().getTimeRange().start_time.date_time+" to "+eventFinderSubsetter.getEventTimeRange().getTimeRange().end_time.date_time);
        
        String source = eventFinderSubsetter.getSourceName();
        String dns =  eventFinderSubsetter.getDNSName();
        EventTimeRange reqTimeRange = eventFinderSubsetter.getEventTimeRange();
        boolean done = false;
        while(!done){
            MicroSecondDate queryStart = reqTimeRange.getStartMSD();
            if(Start.getEventQueue().getTime(source, dns) != null) {//If the database has a start time
                //use the database's start time
                queryStart = new MicroSecondDate(Start.getEventQueue().getTime(source, dns));
            }
            MicroSecondDate queryEnd = incrementTime(queryStart);
            MicroSecondDate requestEnd = reqTimeRange.getEndMSD();
            if(requestEnd.before(queryEnd)){
                queryEnd = requestEnd;
            }
            if(pastNow(queryEnd)){
                queryEnd = ClockUtil.now();
            }
            if(!queryEnd.before(queryStart)){
                EventAccess[] events =
                    finder.query_events(eventFinderSubsetter.getArea(),
                                        minDepth, maxDepth,
                                        new TimeRange(queryStart.getFissuresTime(),
                                                      queryEnd.getFissuresTime()),
                                        searchTypes, minMagnitude, maxMagnitude,
                                        eventFinderSubsetter.getCatalogs(),
                                        eventFinderSubsetter.getContributors(),
                                        10, eventSeqIterHolder);
                logger.debug("Found "+events.length+" events between "+
                                 queryStart + " and "+ queryEnd);
                Start.getEventQueue().setTime(source, dns, queryEnd.getFissuresTime());
                startEventSubsetter(makeCacheEvents(events));
                Start.getEventQueue().waitForProcessing();
            }else if(!isDone(queryEnd, requestEnd)){
                done = true;
            }
        }
        logger.debug("Finished processing the event arm.");
    }
    
    private EventAccessOperations[] makeCacheEvents(EventAccessOperations[] uncached){
        EventAccessOperations[] cached = new EventAccessOperations[uncached.length];
        for(int counter = 0; counter < cached.length; counter++) {
            cached[counter] = new CacheEvent(uncached[counter]);
        }
        return cached;
    }
    
    private void startEventSubsetter(EventAccessOperations[] event)throws Exception{
        for (int i = 0; i < event.length; i++) {
            startEventSubsetter(event[i], event[i].get_attributes());
        }
    }
    
    public void startEventSubsetter(EventAccessOperations event, EventAttr attr) throws Exception {
        begin(event);
        if(eventAttrSubsetter == null || eventAttrSubsetter.accept(attr, null)) {
            Origin origin = event.get_preferred_origin();
            if(originSubsetter.accept(event, origin, null)) {
                Start.getEventQueue().push(eventFinderSubsetter.getDNSName(),
                                           eventFinderSubsetter.getSourceName(),
                                               (EventAccess)((CacheEvent)event).getEventAccess(),
                                           origin);
                pass(event);
                startProcessors(event);
            }else{
                fail(event, "The event origin subsetter did not accept this event");
            }
        }else{
            fail(event, "The event attribute subsetter did not accept this event");
        }
    }
    
    private void begin(EventAccessOperations event) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((EventStatus)it.next()).begin(event);
        }
    }
    private void pass(EventAccessOperations event) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((EventStatus)it.next()).pass(event);
        }
    }
    
    private void fail(EventAccessOperations event, String reason) {
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((EventStatus)it.next()).fail(event, reason);
        }
    }
    
    private void startProcessors(EventAccessOperations eventAccess) throws Exception {
        Iterator it = processors.iterator();
        while(it.hasNext()){
            ((EventArmProcess)it.next()).process(eventAccess, null);
        }
    }
    
    /**
     * this increments the passed in time by the amount of time each event
     * query is set to cover in the edu.sc.seis.sod.daystoincrement property
     *
     */
    private MicroSecondDate incrementTime(MicroSecondDate startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_YEAR, getIncrement());
        MicroSecondDate incrementedDate = new MicroSecondDate(calendar.getTime());
        return incrementedDate;
    }
    
    /**
     * checks to see if the eventArm is done processing for the time interval
     * specified in the configuration file.
     * If finished returns true else returns false.
     */
    private boolean isDone(MicroSecondDate queryStart, MicroSecondDate reqEnd){
        if(firstAfterSecond(queryStart, reqEnd) || pastNow(queryStart)){
            return passivateEventArm(reqEnd);
        }
        return false;
    }
    
    /**
     * Method pastNow checks if time is later than now
     *
     * @return   true if time is later than or equal to now
     *
     */
    private boolean pastNow(MicroSecondDate time){
        return ClockUtil.now().after(time);
    }
    
    /**
     * Method past checks if firstTime is later than second time
     *
     * @return  true if second time is later than or equal to firstTime
     */
    private boolean firstAfterSecond(MicroSecondDate first, MicroSecondDate second){
        if(second.before(first) || second.equals(first)) return true;
        return false;
    }
    
    /***
     * passivates the eventArm (makes it to sleep) based on the refreshInterval
     * and quitTime specified in the property file.
     */
    private boolean passivateEventArm(MicroSecondDate endDate) {
        MicroSecondDate quitDate = endDate.add(new TimeInterval(Start.QUIT_TIME,
                                                                UnitImpl.DAY));
        if(pastNow(quitDate))  return true;
        try {
            logger.debug("Sleep before looking for new events, will sleep for "+
                             Start.REFRESH_INTERVAL+" seconds");
            Thread.sleep(Start.REFRESH_INTERVAL * 1000);
        } catch(InterruptedException ie) {
            logger.warn("Event arm sleep was interrupted.", ie);
        }
        Start.getEventQueue().deleteTimeConfig();
        return false;
    }
    
    /**
     * returns the increment value. This value is specified as a property in the
     * property file.
     */
    
    private int getIncrement() {
        String value = props.getProperty("edu.sc.seis.sod.daystoincrement");
        if(value == null) return 1;
        try {
            int val = Integer.parseInt(value);
            return val;
        } catch(NumberFormatException nfe) {
            return 1;
        }
    }
    
    private EventFinder eventFinderSubsetter;
    
    private EventChannelFinder eventChannelFinder = null;
    
    private EventAttrSubsetter eventAttrSubsetter = new NullEventAttrSubsetter();
    
    private OriginSubsetter originSubsetter = new NullOriginSubsetter();
    
    private List processors = new ArrayList();
    
    private List statusMonitors = new ArrayList();
    
    private Properties props;
    
    private static Logger logger = Logger.getLogger(EventArm.class);
}// EventArm

