package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.eventArm.*;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
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
            CommonAccess.getCommonAccess().handleException(e, "Exception caught while processing the EventArm");

            //notify Exception listeners in case of an exception
            notifyListeners(this, e);
        }
        // if eventChannelFinder is false ... i.e., if no eventChannel is configured
        // then the eventArm can signal end of processing
        // by setting the sourceAlive attribute of the EventQueue to be false.
        if(eventChannelFinder == null) Start.getEventQueue().setSourceAlive(false);
        logger.debug("Event arm finished");
    }

    public void add(EventStatus monitor){ statusMonitors.add(monitor); }

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
                    add((EventStatus)sodElement);
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
        boolean done = false;
        while(!done){
            String source = eventFinderSubsetter.getSourceName();
            String dns =  eventFinderSubsetter.getDNSName();
            logger.debug("DNS for events is " + dns + " source is " + source);
            EventTimeRange reqTimeRange = eventFinderSubsetter.getEventTimeRange();
            MicroSecondDate queryStart = getQueryStart(reqTimeRange, source, dns);
            MicroSecondDate queryEnd = getQueryEnd(queryStart, reqTimeRange.getEndMSD());
            if(queryEnd.after(queryStart)){
                EventAccessOperations[] events = querier.query(new TimeRange(queryStart.getFissuresTime(), queryEnd.getFissuresTime()));
                logger.debug("Found "+events.length+" events between "+ queryStart + " and "+ queryEnd);
                Start.getEventQueue().setTime(source, dns, queryEnd.getFissuresTime());
                startEventSubsetter(events);
                setStatus("Waiting for the wave form queue to process some events before getting more events");
                Start.getEventQueue().waitForProcessing();
            }
            TimeInterval queryLength = queryEnd.subtract(queryStart);
            if(queryLength.lessThan(getIncrement())){
                if(shouldQuit()){
                    done = true;
                }else{
                    waitTillRefreshNeeded();
                    Start.getEventQueue().deleteTimeConfig();//Start event search from beginning
                }
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

    private EventAccessOperations[] cacheEvents(EventAccessOperations[] uncached){
        EventAccessOperations[] cached = new EventAccessOperations[uncached.length];
        for(int counter = 0; counter < cached.length; counter++) {
            if (uncached[counter] instanceof CacheEvent) {
                cached[counter] = uncached[counter];
            } else {
                cached[counter] = new CacheEvent(uncached[counter]);
            }
        }
        return cached;
    }

    private void startEventSubsetter(EventAccessOperations[] event)throws Exception{
        for (int i = 0; i < event.length; i++) {
            startEventSubsetter(event[i], event[i].get_attributes());
        }
    }

    public void startEventSubsetter(EventAccessOperations event, EventAttr attr) throws Exception {
        change(event, RunStatus.NEW);
        if(eventAttrSubsetter == null || eventAttrSubsetter.accept(attr, null)) {
            Origin origin = event.get_preferred_origin();
            if(originSubsetter.accept(event, origin, null)) {
                change(event, RunStatus.PASSED);
                startProcessors(event);
                Start.getEventQueue().push(eventFinderSubsetter.getDNSName(),
                                           eventFinderSubsetter.getSourceName(),
                                               (EventAccess)((CacheEvent)event).getEventAccess(),
                                           origin);
            }else{
                change(event, RunStatus.FAILED);
            }
        }else{
            change(event, RunStatus.FAILED);
        }
    }

    private void change(EventAccessOperations event, RunStatus status) {
        Iterator it = statusMonitors.iterator();
        synchronized(statusMonitors){
            while(it.hasNext()){
                ((EventStatus)it.next()).change(event, status);
            }
        }
    }

    private void setStatus(String status){
        Iterator it = statusMonitors.iterator();
        synchronized(statusMonitors){
            while(it.hasNext()){
                ((EventStatus)it.next()).setArmStatus(status);
            }
        }
    }


    private void startProcessors(EventAccessOperations eventAccess) throws Exception {
        Iterator it = processors.iterator();
        while(it.hasNext()){
            ((EventArmProcess)it.next()).process(eventAccess, null);
        }
    }

    /**
     * @returns true if the EventArm's last desired event time + eventArrivalLag
     * is before the current time
     */
    private boolean shouldQuit(){
        TimeInterval eventArrivalLag = new TimeInterval(7, UnitImpl.DAY);
        MicroSecondDate quitDate = eventFinderSubsetter.getEventTimeRange().getEndMSD().add(eventArrivalLag);
        if(quitDate.before(ClockUtil.now()))  return true;
        return false;
    }

    private void waitTillRefreshNeeded() {
        try {
            logger.debug("Sleep before looking for new events, will sleep for "+
                             Start.REFRESH_INTERVAL+" seconds");
            setStatus("Waiting until " + ClockUtil.now().add(new TimeInterval(Start.REFRESH_INTERVAL, UnitImpl.SECOND)) + " to check for new events");
            Thread.sleep(Start.REFRESH_INTERVAL * 1000);
        } catch(InterruptedException ie) {
            logger.warn("Event arm sleep was interrupted.", ie);
        }
    }

    /**
     * returns the increment value. This value is specified as
     * edu.sc.seis.sod.daystoincrement in the property file.
     */
    private TimeInterval getIncrement() {
        if(increment == null){
            int val = 1;
            String value = props.getProperty("edu.sc.seis.sod.daystoincrement");
            if(value != null){
                try {
                    val = Integer.parseInt(value);
                } catch(NumberFormatException nfe) {
                    CommonAccess.getCommonAccess().handleException(nfe, "The daystoincrement property does not appear to be a number: "+value);
                }
            }
            increment = new TimeInterval(val, UnitImpl.DAY);
        }
        return increment;
    }

    private class Querier{
        public Querier() throws ConfigurationException{
            if(eventFinderSubsetter.getDepthRange() != null) {
                minDepth = eventFinderSubsetter.getDepthRange().getMinDepth();
                maxDepth = eventFinderSubsetter.getDepthRange().getMaxDepth();
                logger.debug("depth range for event search is in subsetter, min of " + minDepth + " max of " + maxDepth);
            }else{
                logger.debug("Using default depth range for event search");
            }
            if(eventFinderSubsetter.getMagnitudeRange() != null) {
                MagnitudeRange magRange = eventFinderSubsetter.getMagnitudeRange();
                minMag = magRange.getMinMagnitude().value;
                maxMag = magRange.getMaxMagnitude().value;
                searchTypes = magRange.getSearchTypes();
                logger.debug("mag range is in subsetter, min of " + minMag + " max of " + maxMag);
            }else{
                logger.debug("Using default mag ranges for event search");
            }
            logger.debug("Searching over area " + area + " in catalogs " + catalogs[0] + " from contributors " + contributors[0]);
        }

        public EventAccessOperations[] query(TimeRange tr) throws Exception{
            edu.iris.Fissures.IfEvent.EventFinder finder = eventFinderSubsetter.getEventDC().a_finder();
            logger.debug("before finder.query_events("+tr.start_time.date_time+" to "+tr.end_time.date_time);
            EventAccessOperations[] events =  finder.query_events(area,
                                                                  minDepth, maxDepth,
                                                                  tr,
                                                                  searchTypes, minMag, maxMag,
                                                                  catalogs,
                                                                  contributors,
                                                                  sequenceMaximum, holder);
            logger.debug("after finder.query_events("+tr.start_time.date_time+" to "+tr.end_time.date_time);
            events = cacheEvents(events);
            return events;
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

    private TimeInterval increment;

    private EventFinder eventFinderSubsetter;

    private EventChannelFinder eventChannelFinder = null;

    private EventAttrSubsetter eventAttrSubsetter = new NullEventAttrSubsetter();

    private OriginSubsetter originSubsetter = new NullOriginSubsetter();

    private List processors = new ArrayList();

    private List statusMonitors = Collections.synchronizedList(new ArrayList());

    private Properties props;

    private static Logger logger = Logger.getLogger(EventArm.class);
}// EventArm

