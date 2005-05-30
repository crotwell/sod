package edu.sc.seis.sod;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAccessSeqHolder;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventSeqIter;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventLoader;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.database.JDBCQueryTime;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.status.OutputScheduler;
import edu.sc.seis.sod.status.eventArm.EventMonitor;
import edu.sc.seis.sod.subsetter.origin.EventFinder;
import edu.sc.seis.sod.subsetter.origin.MagnitudeRange;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;
import edu.sc.seis.sod.subsetter.origin.OriginTimeRange;

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
        refreshInterval = Start.getRunProps().getEventRefreshInterval();
        lag = Start.getRunProps().getEventLag();
        increment = Start.getRunProps().getEventQueryIncrement();
        try {
            eventStatus = new JDBCEventStatus();
            queryTimes = new JDBCQueryTime();
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
                if((el).getTagName().equals("description"))
                    continue;
                Object sodElement = SodUtil.load(el, new String[] {"eventArm",
                                                                   "origin"});
                if(sodElement instanceof EventFinder) {
                    finder = (EventFinder)sodElement;
                } else if(sodElement instanceof OriginSubsetter) {
                    subsetters.add(sodElement);
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    private void getEvents() throws Exception {
        if(finder == null)
            return;
        logger.debug("getting events from "
                + finder.getEventTimeRange().getMSTR());
        Querier querier = new Querier(finder);
        boolean done = false;
        String server = finder.getSourceName();
        String dns = finder.getDNSName();
        logger.debug("DNS for events is " + dns + " source is " + server);
        OriginTimeRange reqTimeRange = finder.getEventTimeRange();
        while(!done) {
            waitForProcessing();
            MicroSecondDate queryStart = getQueryStart(reqTimeRange,
                                                       server,
                                                       dns);
            MicroSecondDate queryEnd = getQueryEnd(queryStart,
                                                   reqTimeRange.getEndMSD());
            int numRetries = 0;
            if(queryEnd.after(queryStart)) {
                boolean needRetry = true;
                while(needRetry) {
                    try {
                        CacheEvent[] events = querier.query(new TimeRange(queryStart.getFissuresTime(),
                                                                          queryEnd.getFissuresTime()));
                        logger.debug("Found " + events.length
                                + " events between " + queryStart + " and "
                                + queryEnd);
                        //Scarab: Sod8
                        //We have the source and dns here. If we wanted to go
                        //back to the server to refresh the events instead of
                        //using the cached copy, should probably start here
                        handle(events);
                        queryTimes.setQuery(server, dns, queryEnd);
                        setStatus("Waiting for the wave form queue to process some events before getting more events");
                        needRetry = false;
                    } catch(org.omg.CORBA.SystemException e) {
                        // an SystemException signals an error on the server.
                        // This should
                        // go away once the server is fixed/restarted, so we
                        // log the error and sleep before retrying
                        numRetries++;
                        // force trip back to name service
                        finder.forceGetEventDC();
                        GlobalExceptionHandler.handle("Got an Corba exception while trying query from "
                                                              + queryStart.getFissuresTime().date_time
                                                              + " to "
                                                              + queryEnd.getFissuresTime().date_time
                                                              + ", sleep for 1 minute before retrying. Num retries = "
                                                              + numRetries,
                                                      e);
                        try {
                            Thread.sleep(60 * 1000);
                        } catch(InterruptedException ee) {}
                    }
                }
            }
            TimeInterval queryLength = queryEnd.subtract(queryStart);
            if(queryLength.lessThan(increment)) {
                if(shouldQuit()) {
                    done = true;
                } else {
                    waitTillRefreshNeeded();
                    resetQueryTimeForLag(server,
                                         dns,
                                         reqTimeRange.getStartMSD());
                }
            }
        }
        logger.debug("Finished processing the event arm.");
    }

    public EventAccessOperations getLastEvent() {
        return lastEvent;
    }

    private void resetQueryTimeForLag(String server,
                                      String dns,
                                      MicroSecondDate queryStart) {
        try {
            MicroSecondDate curEnd = new MicroSecondDate(queryTimes.getQuery(server,
                                                                             dns));
            MicroSecondDate newEnd = curEnd.subtract(lag);
            if(queryStart.after(newEnd))
                newEnd = queryStart;
            queryTimes.setQuery(server, dns, newEnd);
        } catch(SQLException e) {
            GlobalExceptionHandler.handle("The query time table just threw this SQLException.  This shouldn't happen.  Something nasty is probably happening to the database now",
                                          e);
        } catch(edu.sc.seis.fissuresUtil.database.NotFound e) {
            GlobalExceptionHandler.handle("The query times database threw a not found for the event server which is nonsensical as we just inserted it above here.  Something is probably very screwy",
                                          e);
        }
    }

    private void waitForProcessing() throws SQLException {
        if(waitForWaveformProcessing) {
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

    private MicroSecondDate getQueryStart(OriginTimeRange reqTimeRange,
                                          String source,
                                          String dns) {
        MicroSecondDate queryStart = reqTimeRange.getStartMSD();
        try {
            if(queryTimes.getQuery(source, dns) != null) {//If the database has
                // a end time, use the
                // database's end time
                queryStart = queryTimes.getQuery(source, dns);
            }
        } catch(SQLException e) {
            throw new RuntimeException("Trouble with the SQL for getting event server query times!");
        } catch(edu.sc.seis.fissuresUtil.database.NotFound e) {
            logger.debug("the query times database didn't have an entry for our server/dns combo, just use the time int he config file");
        }
        return queryStart;
    }

    private MicroSecondDate getQueryEnd(MicroSecondDate queryStart,
                                        MicroSecondDate reqEnd) {
        MicroSecondDate queryEnd = queryStart.add(increment);
        if(reqEnd.before(queryEnd)) {
            queryEnd = reqEnd;
        }
        if(ClockUtil.now().before(queryEnd)) {
            queryEnd = ClockUtil.now();
        }
        return queryEnd;
    }

    private CacheEvent[] cacheEvents(EventAccessOperations[] uncached) {
        CacheEvent[] cached = new CacheEvent[uncached.length];
        for(int counter = 0; counter < cached.length; counter++) {
            if(uncached[counter] instanceof CacheEvent) {
                cached[counter] = (CacheEvent)uncached[counter];
            } else {
                cached[counter] = new CacheEvent(uncached[counter]);
                // preload cache
                EventLoader backLoader = new EventLoader(cached[counter]);
                WorkerThreadPool.getDefaultPool().invokeLater(backLoader);
            }
        }
        return cached;
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
     * logging without causeing further exceptions.
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
        Iterator it = statusMonitors.iterator();
        synchronized(statusMonitors) {
            while(it.hasNext()) {
                ((EventMonitor)it.next()).setArmStatus(status);
            }
        }
    }

    /**
     * @returns true if the EventArm's last desired event time + eventArrivalLag
     *          is before the current time
     */
    private boolean shouldQuit() {
        MicroSecondDate quitDate = finder.getEventTimeRange()
                .getEndMSD()
                .add(lag);
        if(quitDate.before(ClockUtil.now()))
            return true;
        return false;
    }

    private void waitTillRefreshNeeded() throws Exception {
        try {
            logger.debug("Sleep before looking for new events, will sleep for "
                    + refreshInterval);
            setStatus("Waiting until " + ClockUtil.now().add(refreshInterval)
                    + " to check for new events");
            Thread.sleep((long)refreshInterval.convertTo(UnitImpl.MILLISECOND)
                    .getValue());
        } catch(InterruptedException ie) {
            logger.warn("Event arm sleep was interrupted.", ie);
        }
    }

    private class Querier {

        public Querier(EventFinder ef) {
            if(ef.getDepthRange() != null) {
                minDepth = ef.getDepthRange().getMinDepth();
                maxDepth = ef.getDepthRange().getMaxDepth();
                logger.debug("depth range for event search is in subsetter, min of "
                        + minDepth + " max of " + maxDepth);
            } else {
                logger.debug("Using default depth range for event search");
            }
            if(ef.getMagnitudeRange() != null) {
                MagnitudeRange magRange = ef.getMagnitudeRange();
                minMag = magRange.getMinValue();
                maxMag = magRange.getMaxValue();
                searchTypes = magRange.getSearchTypes();
                String s = "mag range is in subsetter, min of " + minMag
                        + " max of " + maxMag + " types= ";
                for(int i = 0; i < searchTypes.length; i++) {
                    s += i + "=" + searchTypes[i] + ", ";
                }
                logger.debug(s);
            } else {
                logger.debug("Using default mag ranges for event search");
            }
            String s = "Searching over area " + area + " in catalogs ";
            for(int i = 0; i < catalogs.length; i++) {
                s += i + "=" + catalogs[i] + ", ";
            }
            s += " from contributors ";
            for(int i = 0; i < contributors.length; i++) {
                s += i + "=" + contributors[i] + ", ";
            }
            logger.debug(s);
        }

        public CacheEvent[] query(TimeRange tr) {
            for(int i = 0; i < MAX_RETRY; i++) {
                try {
                    edu.iris.Fissures.IfEvent.EventFinder evFinder = finder.getEventDC()
                            .a_finder();
                    logger.debug("before finder.query_events("
                            + tr.start_time.date_time + " to "
                            + tr.end_time.date_time);
                    EventAccessOperations[] events = evFinder.query_events(area,
                                                                           minDepth,
                                                                           maxDepth,
                                                                           tr,
                                                                           searchTypes,
                                                                           (float)minMag,
                                                                           (float)maxMag,
                                                                           catalogs,
                                                                           contributors,
                                                                           sequenceMaximum,
                                                                           holder);
                    logger.debug("after finder.query_events("
                            + tr.start_time.date_time + " to "
                            + tr.end_time.date_time + " got " + events.length);
                    if(holder.value != null) {
                        // might be events in the iterator...
                        LinkedList allEvents = new LinkedList();
                        for(int j = 0; j < events.length; j++) {
                            allEvents.add(events[j]);
                        }
                        EventSeqIter iterator = holder.value;
                        EventAccessSeqHolder eHolder = new EventAccessSeqHolder();
                        while(iterator.how_many_remain() > 0) {
                            iterator.next_n(sequenceMaximum, eHolder);
                            EventAccess[] iterEvents = eHolder.value;
                            for(int j = 0; j < iterEvents.length; j++) {
                                allEvents.add(iterEvents[j]);
                            }
                        }
                        events = (EventAccessOperations[])allEvents.toArray(new EventAccessOperations[0]);
                    }
                    return cacheEvents(events);
                } catch(org.omg.CORBA.SystemException e) {
                    if(i == MAX_RETRY - 1) {
                        // too many retries
                        throw e;
                    } else {
                        // maybe it will be ok if we retry, log just in case
                        GlobalExceptionHandler.handle("Got a corba exception querying, retrying "
                                                              + i
                                                              + " of "
                                                              + MAX_RETRY,
                                                      e);
                        try {
                            // sleep for 1 second just to give things a chance
                            // to change
                            // hope springs eternal...
                            Thread.sleep(1000);
                        } catch(InterruptedException ex) {
                            // oh well...
                        }
                    }
                }
            }
            // should never get here
            throw new RuntimeException();
        }

        private static final int MAX_RETRY = 3;

        //If the eventFinderSubsetter values for magnitude or depth are null,
        //the default values below are used
        private Quantity minDepth = new QuantityImpl(-90000.0,
                                                     UnitImpl.KILOMETER);

        private Quantity maxDepth = new QuantityImpl(90000.0,
                                                     UnitImpl.KILOMETER);

        private String[] searchTypes = {"%"};

        private double minMag = -99.0f, maxMag = 99.0f;

        private Area area = finder.getArea();

        private String[] catalogs = finder.getCatalogs();

        private String[] contributors = finder.getContributors();

        private int sequenceMaximum = 100;

        private EventSeqIterHolder holder = new EventSeqIterHolder();
    }

    private static final Status IN_PROG = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                     Standing.IN_PROG);

    private static final Status SUCCESS = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                     Standing.SUCCESS);

    private TimeInterval increment, lag, refreshInterval;

    private EventFinder finder;

    private List subsetters = new ArrayList();

    private List statusMonitors = Collections.synchronizedList(new ArrayList());

    private JDBCEventStatus eventStatus;

    private JDBCQueryTime queryTimes;

    private boolean alive = true;

    private boolean waitForWaveformProcessing = true;

    private CacheEvent lastEvent;

    private static Logger logger = Logger.getLogger(EventArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.EventArm");

    public EventFinder getFinder() {
        return finder;
    }
}// EventArm
