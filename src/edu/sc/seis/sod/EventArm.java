package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.source.event.EventSource;
import edu.sc.seis.sod.status.OutputScheduler;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.eventArm.EventMonitor;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;

/**
 * This class handles the subsetting of the Events based on the subsetters
 * specified in the configuration file (xml file). Created: Thu Mar 14 14:09:52
 * 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public class EventArm implements Arm {

    public EventArm() throws ConfigurationException {
        this(null, true);
    }
    public EventArm(Element config) throws ConfigurationException {
        this(config, true);
    }

    public EventArm(Element config, boolean waitForWaveformProcessing)
            throws ConfigurationException {
        this.waitForWaveformProcessing = waitForWaveformProcessing;
        eventStatus = new StatefulEventDB();
        if (config != null) {
        processConfig(config);
        }
    }

    public boolean isActive() {
        return alive;
    }
    
    public String getName() {
        return "EventArm";
    }

    public void run() {
        try {
            for(Iterator iter = sources.iterator(); iter.hasNext();) {
                EventSource source = (EventSource)iter.next();
                logger.debug(source + " covers events from "
                        + source.getEventTimeRange());
            }
            
            while( !Start.isArmFailure() && atLeastOneSourceHasNext()) {
                getEvents();
            }
            logger.debug("Finished processing the event arm.");
        } catch(Throwable e) {
            Start.armFailure(this, e);
        }
        logger.info("Event arm finished");
        alive = false;
        synchronized(getWaveformArmSync()) {
            getWaveformArmSync().notifyAll();
        }
        synchronized(OutputScheduler.getDefault()) {
            OutputScheduler.getDefault().notifyAll();
        }
    }


    public void add(EventMonitor monitor) {
        statusMonitors.add(monitor);
    }

    private void processConfig(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                Element el = (Element)node;
                Object sodElement = SodUtil.load(el, new String[] {"eventArm",
                                                                   "origin",
                                                                   "event"});
                if(sodElement instanceof EventSource) {
                    sources.add((EventSource)sodElement);
                } else if(sodElement instanceof OriginSubsetter) {
                    subsetters.add((OriginSubsetter)sodElement);
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    private void getEvents() throws Exception {
            Iterator it = sources.iterator();
            while(it.hasNext()) {
                EventSource source = (EventSource)it.next();
                TimeInterval wait = source.getWaitBeforeNext();
                if ((lastTime.get(source) == null || lastTime.get(source).add(wait).before(ClockUtil.now())) && source.hasNext()) {
                    CacheEvent[] next = source.next();
                    logger.debug("Handling " + next.length + " events from "+source.getDescription());
                    handle(next);
                    lastTime.put(source, ClockUtil.now());
                    waitForProcessing();
                    if (waitForWaveformProcessing && Start.isArmFailure()) {
                        // we are supposed to wait for the waveform arm to process, but
                        // if it is no longer active due to an arm failure, then we should not wait forever
                        logger.warn("Arm failure, "+getName()+" exiting early");
                        return;
                    }
                }
                if (lastTime.get(source) == null) {
                    lastTime.put(source, ClockUtil.now());
                }
            }
            it = sources.iterator();
            TimeInterval minWait = null;
            while(it.hasNext()) {
                EventSource source = (EventSource)it.next();
                if (source.hasNext() && lastTime.get(source) != null) {
                    TimeInterval tmpWait = lastTime.get(source).add(source.getWaitBeforeNext()).subtract(ClockUtil.now());
                    if (minWait == null || tmpWait.lessThan(minWait)) {
                        minWait = tmpWait;
                    }
                }
            }
            if (minWait != null) {
                logger.debug("Wait before next getEvents: "+minWait);
                long waitMillis = (long)minWait.convertTo(UnitImpl.MILLISECOND).get_value();
                if(waitMillis > 0) {
                    try {
                        setStatus("Waiting until "
                                  + ClockUtil.now().add(minWait)
                                  + " to check for new events");
                        synchronized(this) {
                            wait(waitMillis);
                        }
                    } catch(InterruptedException e) {}
                }
            }
            
    }

    private boolean atLeastOneSourceHasNext() {
        Iterator it = sources.iterator();
        while(it.hasNext()) {
            if(((EventSource)it.next()).hasNext()) {
                return true;
            }
        }
        return false;
    }

    public EventAccessOperations getLastEvent() {
        return lastEvent;
    }

    static int MIN_WAIT_EVENTS = 10;
    
    private void waitForProcessing() throws Exception {
        int numWaiting = eventStatus.getNumWaiting();
        logger.debug("Event wait: numWaiting = "+numWaiting+" should be < "+MIN_WAIT_EVENTS);
        while( !Start.isArmFailure() && waitForWaveformProcessing  &&  numWaiting > MIN_WAIT_EVENTS ) {
            synchronized(this) {
                setStatus("eventArm waiting until there are less than "+MIN_WAIT_EVENTS+" events waiting to be processed. "+numWaiting+" in queue now.");
                synchronized(getWaveformArmSync()) {
                    getWaveformArmSync().notifyAll();
                }
                wait();
            }
            numWaiting = eventStatus.getNumWaiting();
        }
        // less events, but maybe lots of event-network pairs
        int numENPWaiting = SodDB.getSingleton().getNumEventNetworkWorkUnits(Standing.INIT);
        while( !Start.isArmFailure() && waitForWaveformProcessing  &&  numWaiting+numENPWaiting > MIN_WAIT_EVENTS ) {
            synchronized(this) {
                setStatus("eventArm waiting until there are less than "+MIN_WAIT_EVENTS
                          +" events and event-network pairs waiting to be processed, "+(numWaiting+numENPWaiting)+" in queue now.");
                synchronized(getWaveformArmSync()) {
                    getWaveformArmSync().notifyAll();
                }
                wait();
            }
            numENPWaiting = SodDB.getSingleton().getNumEventNetworkWorkUnits(Standing.INIT);
        }
        logger.debug("event arm getting more events, numWaiting:"+numWaiting+" numENPWaiting:"+numENPWaiting);
    }

    public void handle(CacheEvent[] events) {
        for(int i = 0; i < events.length; i++) {
            try {
                handle(events[i]);
                eventStatus.commit();
                synchronized(getWaveformArmSync()) {
                    getWaveformArmSync().notifyAll();
                }
            } catch(HibernateException e) {
                eventStatus.rollback();
                handleException(e, events[i], i);
            } catch(Exception e) {
                handleException(e, events[i], i);
            } catch(Throwable e) {
                handleException(e, events[i], i);
            }
        }
    }
    
    private void handleException(Throwable t, CacheEvent event, int i) {
        // problem with this event, log it and go on
        GlobalExceptionHandler.handle("Caught an exception for event "
                + i + " " + bestEffortEventToString(event)
                + " Continuing with rest of events", t);
    }

    /**
     * This exists so that we can try getting more info about an event for the
     * logging without causing further exceptions.
     */
    private String bestEffortEventToString(EventAccessOperations event) {
        String s = "";
        try {
            Origin o = event.get_preferred_origin();
            s = " otime=" + o.getOriginTime().date_time;
            s += " loc=" + o.getLocation().latitude + ", "
                    + o.getLocation().longitude;
        } catch(Throwable e) {
            s += e;
        }
        return s;
    }

    static Status EVENT_IN_PROG = Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
            Standing.IN_PROG);
    static Status EVENT_REJECT = Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
            Standing.REJECT);
    
    private void handle(CacheEvent event) throws Exception {
        logger.debug("Handle: "+event);
        StatefulEvent storedEvent = eventStatus.getIdenticalEvent(event);
        if(storedEvent == null ) {
        	storedEvent = new StatefulEvent(event,  EVENT_IN_PROG);
        	eventStatus.put(storedEvent);
            change(storedEvent);
        } else {
            // already in database, so don't need to try it again
            return;
        }
        if (storedEvent.getStatus() != ECPOP_INIT && storedEvent.getStatus() != SUCCESS) {
        	storedEvent.setStatus(EVENT_IN_PROG);
            change(storedEvent);
            Iterator it = subsetters.iterator();
            while(it.hasNext()) {
                OriginSubsetter cur = (OriginSubsetter)it.next();
                StringTree result = cur.accept(event, event.get_attributes(), event.getOrigin());
                if(!result.isSuccess()) {
                	storedEvent.setStatus(EVENT_REJECT);
                    change(storedEvent);
                    failLogger.info(event + " " + result);
                    return;
                }
            }
            storedEvent.setStatus(ECPOP_INIT);
            change(storedEvent);
            lastEvent = event;
        }
    }

    public void change(StatefulEvent event) {
        Iterator it = statusMonitors.iterator();
        synchronized(statusMonitors) {
            while(it.hasNext()) {
                ((EventMonitor)it.next()).change(event, event.getStatus());
            }
        }
    }

    public void setWaitForWaveformProcessing(boolean b) {
        this.waitForWaveformProcessing = b;
    }

    private void setStatus(String status) throws Exception {
        logger.debug(status);
        Iterator it = statusMonitors.iterator();
        synchronized(statusMonitors) {
            while(it.hasNext()) {
                ((EventMonitor)it.next()).setArmStatus(status);
            }
        }
    }

    public EventSource[] getSources() {
        return (EventSource[])sources.toArray(new EventSource[0]);
    }

    public Object getWaveformArmSync() {
        return waveformArmSync;
    }
    
    private static final Status ECPOP_INIT = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                     Standing.INIT);

    private static final Status SUCCESS = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                     Standing.SUCCESS);

    private final Object waveformArmSync = new Object();
    
    private HashMap<EventSource, MicroSecondDate> lastTime = new HashMap<EventSource, MicroSecondDate>();
    
    private List<EventSource> sources = new ArrayList<EventSource>();

    private List<OriginSubsetter> subsetters = new ArrayList<OriginSubsetter>();

    private List statusMonitors = Collections.synchronizedList(new ArrayList());

    private StatefulEventDB eventStatus;

    private boolean alive = true;

    private boolean waitForWaveformProcessing = true;

    private CacheEvent lastEvent;

    private static Logger logger = Logger.getLogger(EventArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.EventArm");

    
}// EventArm
