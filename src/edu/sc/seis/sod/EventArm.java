package edu.sc.seis.sod;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.source.event.EventSource;
import edu.sc.seis.sod.status.OutputScheduler;
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

    public EventArm(Element config) throws ConfigurationException {
        this(config, true);
    }

    public EventArm(Element config, boolean waitForWaveformProcessing)
            throws ConfigurationException {
        this.waitForWaveformProcessing = waitForWaveformProcessing;
        try {
            eventStatus = new JDBCEventStatus();
        } catch(SQLException e) {
            throw new RuntimeException("Trouble setting up event status database",
                                       e);
        }
        processConfig(config);
    }

    public boolean isActive() {
        return alive;
    }

    public void run() {
        try {
            getEvents();
        } catch(Throwable e) {
            GlobalExceptionHandler.handle("Exception caught while processing the EventArm",
                                          e);
        }
        logger.debug("Event arm finished");
        alive = false;
        synchronized(OutputScheduler.getDefault()) {
            OutputScheduler.getDefault().notify();
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
                    source = (EventSource)sodElement;
                } else if(sodElement instanceof OriginSubsetter) {
                    subsetters.add(sodElement);
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    private void getEvents() throws Exception {
        logger.debug("getting events from " + source.getEventTimeRange());
        while(source.hasNext()) {
            TimeInterval wait = source.getWaitBeforeNext();
            long waitMillis = (long)wait.convertTo(UnitImpl.MILLISECOND)
                    .get_value();
            if(waitMillis > 0) {
                try {
                    setStatus("Waiting until " + ClockUtil.now().add(wait)
                            + " to check for new events");
                    Thread.sleep(waitMillis);
                } catch(InterruptedException e) {}
            }
            handle(source.next());
            waitForProcessing();
        }
        logger.debug("Finished processing the event arm.");
    }

    public EventAccessOperations getLastEvent() {
        return lastEvent;
    }

    private void waitForProcessing() throws Exception {
        if(waitForWaveformProcessing) {
            setStatus("Waiting until there are less than 10 events waiting to be processed.");
            int numEvents;
            while(true) {
                synchronized(eventStatus) {
                    numEvents = eventStatus.getAll(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                              Standing.IN_PROG)).length;
                }
                if(numEvents < 10) {
                    return;
                }
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e) {}
            }
        }
    }

    private void handle(CacheEvent[] events) {
        for(int i = 0; i < events.length; i++) {
            try {
                handle(events[i]);
            } catch(Exception e) {
                // problem with this event, log it and go on
                GlobalExceptionHandler.handle("Caught an exception for event "
                        + i + " " + bestEffortEventToString(events[i])
                        + " Continuing with rest of events", e);
            } catch(Throwable e) {
                // problem with this event, log it and go on
                GlobalExceptionHandler.handle("Caught an exception for event "
                        + i + " " + bestEffortEventToString(events[i])
                        + " Continuing with rest of events", e);
            }
        }
    }

    /**
     * This exists so that we can try getting more info about an event for the
     * logging without causing further exceptions.
     */
    private String bestEffortEventToString(EventAccessOperations event) {
        String s = "";
        try {
            Origin o = event.get_preferred_origin();
            s = " otime=" + o.origin_time.date_time;
            s += " loc=" + o.my_location.latitude + ", "
                    + o.my_location.longitude;
        } catch(Throwable e) {
            s += e;
        }
        return s;
    }

    private void handle(CacheEvent event) throws Exception {
        if(!hasAlreadyPassed(event)) {
            change(event, Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
                                     Standing.IN_PROG));
            EventAttr attr = event.get_attributes();
            Iterator it = subsetters.iterator();
            while(it.hasNext()) {
                OriginSubsetter cur = (OriginSubsetter)it.next();
                if(!cur.accept(event, attr, event.getOrigin())) {
                    Status status = Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
                                               Standing.REJECT);
                    change(event, status);
                    failLogger.info(event + " " + status);
                    return;
                }
            }
            change(event, IN_PROG);
            lastEvent = event;
        }
    }

    private boolean hasAlreadyPassed(CacheEvent event) throws SQLException,
            edu.sc.seis.fissuresUtil.database.NotFound {
        int dbId = eventStatus.getDbId(event);
        if(dbId != -1) {
            Status status = eventStatus.getStatus(dbId);
            if(status == IN_PROG || status == SUCCESS) {
                return true;
            }
        }
        return false;
    }

    public void change(EventAccessOperations event, Status status)
            throws Exception {
        synchronized(eventStatus) {
            eventStatus.setStatus(event, status);
        }
        Iterator it = statusMonitors.iterator();
        synchronized(statusMonitors) {
            while(it.hasNext()) {
                ((EventMonitor)it.next()).change(event, status);
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

    public EventSource getSource() {
        return source;
    }

    private static final Status IN_PROG = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                     Standing.IN_PROG);

    private static final Status SUCCESS = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                     Standing.SUCCESS);

    private EventSource source;

    private List subsetters = new ArrayList();

    private List statusMonitors = Collections.synchronizedList(new ArrayList());

    private JDBCEventStatus eventStatus;

    private boolean alive = true;

    private boolean waitForWaveformProcessing = true;

    private CacheEvent lastEvent;

    private static Logger logger = Logger.getLogger(EventArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.EventArm");
}// EventArm
