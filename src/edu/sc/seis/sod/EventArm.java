package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;
import edu.sc.seis.sod.subsetter.eventArm.*;
import java.util.*;

import edu.iris.Fissures.Area;
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
        // if eventChannelFinder is false ... i.e., if no eventChannel is configured
        // then the eventArm can signal end of processing
        // by setting the sourceAlive attribute of the EventQueue to be false.
        if(eventChannelFinder == null) Start.getEventQueue().setSourceAlive(false);
        logger.debug("Event arm finished");
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
            Thread thread = new Thread(eventChannelFinder, "EventChannelFinder");
            thread.start();
        } else {
            logger.info("EventChannelFinder is not being used.");
        }
        if(eventFinderSubsetter == null) return;
        logger.info("Event Finder query is being used");
        logger.debug("getting events from "+eventFinderSubsetter.getEventTimeRange().getMSTR());
        Querier querier = new Querier();
        System.out.println("created querier");
        boolean done = false;
        while(!done){
            String source = eventFinderSubsetter.getSourceName();
            String dns =  eventFinderSubsetter.getDNSName();
            EventTimeRange reqTimeRange = eventFinderSubsetter.getEventTimeRange();
            MicroSecondDate queryStart = getQueryStart(reqTimeRange, source, dns);
            MicroSecondDate queryEnd = getQueryEnd(queryStart, reqTimeRange.getEndMSD());
            if(queryEnd.after(queryStart)){
                EventAccess[] events = querier.query(new TimeRange(queryStart.getFissuresTime(), queryEnd.getFissuresTime()));
                logger.debug("Found "+events.length+" events between "+ queryStart + " and "+ queryEnd);
                Start.getEventQueue().setTime(source, dns, queryEnd.getFissuresTime());
                startEventSubsetter(cacheEvents(events));
                Start.getEventQueue().waitForProcessing();
            }
            TimeInterval queryLength = queryEnd.subtract(queryStart);
            if(queryLength.lessThan(getIncrement()) && passivateEventArm(reqTimeRange.getEndMSD())){
                done = true;
            }
        }
        logger.debug("Finished processing the event arm.");
    }
    
    private MicroSecondDate getQueryStart(EventTimeRange reqTimeRange, String source, String dns) {
        MicroSecondDate queryStart = reqTimeRange.getStartMSD();
        if(Start.getEventQueue().getTime(source, dns) != null) {//If the database has a start time, use the database's start time
            queryStart = new MicroSecondDate(Start.getEventQueue().getTime(source, dns));
        }
        return queryStart;
    }
    
    private MicroSecondDate getQueryEnd(MicroSecondDate queryStart, MicroSecondDate reqEnd) {
        MicroSecondDate queryEnd = queryStart.add(getIncrement());
        if(reqEnd.before(queryEnd)){
            queryEnd = reqEnd;
        }
        if(ClockUtil.now().before(queryEnd)){
            queryEnd = ClockUtil.now();
        }
        return queryEnd;
    }
    
    private class Querier{
        public Querier() throws ConfigurationException{
            if(eventFinderSubsetter.getDepthRange() != null) {
                minDepth = eventFinderSubsetter.getDepthRange().getMinDepth();
                maxDepth = eventFinderSubsetter.getDepthRange().getMaxDepth();
            }
            if(eventFinderSubsetter.getMagnitudeRange() != null) {
                MagnitudeRange magRange = eventFinderSubsetter.getMagnitudeRange();
                minMag = magRange.getMinMagnitude().value;
                maxMag = magRange.getMaxMagnitude().value;
                searchTypes = magRange.getSearchTypes();
            }
        }
        
        public EventAccess[] query(TimeRange tr) throws Exception{
            edu.iris.Fissures.IfEvent.EventFinder finder = eventFinderSubsetter.getEventDC().a_finder();
            return finder.query_events(area, minDepth, maxDepth, tr,
                                       searchTypes, minMag, maxMag, catalogs,
                                       contributors, sequenceMaximum, holder);
        }
        
        //If the eventFinderSubsetter values for magnitude or depth are null,
        //the default values below are used
        private Quantity minDepth = new QuantityImpl(-90000.0, UnitImpl.KILOMETER);
        private Quantity maxDepth = new QuantityImpl(90000.0, UnitImpl.KILOMETER);
        private String[] searchTypes = { "%" };
        private float minMag = -99.0f, maxMag = 99.0f;
        
        private Area area = eventFinderSubsetter.getArea();
        private String[] catalogs = eventFinderSubsetter.getCatalogs();
        private String[] contributors = eventFinderSubsetter.getContributors();
        private int sequenceMaximum = 10;
        private EventSeqIterHolder holder = new EventSeqIterHolder();
    }
    
    private EventAccessOperations[] cacheEvents(EventAccessOperations[] uncached){
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
     * checks to see if the eventArm is done processing for the time interval
     * specified in the configuration file.
     * If finished returns true else returns false.
     */
    private boolean isDone(MicroSecondDate queryEnd, MicroSecondDate reqEnd){
        if(ClockUtil.now().before(queryEnd)){
            return passivateEventArm(reqEnd);
        }
        return false;
    }
    
    /***
     * passivates the eventArm (makes it to sleep) based on the refreshInterval
     * and quitTime specified in the property file.
     */
    private boolean passivateEventArm(MicroSecondDate endDate) {
        TimeInterval eventArrivalLag = new TimeInterval(7, UnitImpl.DAY);
        MicroSecondDate quitDate = endDate.add(eventArrivalLag);
        if(quitDate.before(ClockUtil.now()))  return true;
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
    
    private TimeInterval getIncrement() {
        if(increment == null){
            int val = 1;
            String value = props.getProperty("edu.sc.seis.sod.daystoincrement");
            if(value != null){
                try {
                    val = Integer.parseInt(value);
                } catch(NumberFormatException nfe) {}
            }
            increment = new TimeInterval(val, UnitImpl.DAY);
        }
        return increment;
    }
    
    private TimeInterval increment;
    
    private EventFinder eventFinderSubsetter;
    
    private EventChannelFinder eventChannelFinder = null;
    
    private EventAttrSubsetter eventAttrSubsetter = new NullEventAttrSubsetter();
    
    private OriginSubsetter originSubsetter = new NullOriginSubsetter();
    
    private List processors = new ArrayList();
    
    private List statusMonitors = new ArrayList();
    
    private Properties props;
    
    private static Logger logger = Logger.getLogger(EventArm.class);
}// EventArm

