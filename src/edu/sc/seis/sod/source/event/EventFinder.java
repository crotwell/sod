package edu.sc.seis.sod.source.event;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;
import org.w3c.dom.Element;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.JDBCQueryTime;
import edu.sc.seis.sod.subsetter.AbstractSource;
import edu.sc.seis.sod.subsetter.origin.OriginTimeRange;

public class EventFinder extends AbstractSource implements EventSource {

    public EventFinder(Element config) throws Exception {
        super(config);
        processConfig(config);
        queryTimes = new JDBCQueryTime();
        refreshInterval = Start.getRunProps().getEventRefreshInterval();
        lag = Start.getRunProps().getEventLag();
        increment = Start.getRunProps().getEventQueryIncrement();
    }

    protected void processConfig(Element config) throws ConfigurationException {
        Element queryTimeEl = DOMHelper.extractElement(config,
                                                       "originTimeRange");
        eventTimeRange = ((OriginTimeRange)SodUtil.load(queryTimeEl,
                                                        new String[] {"eventArm",
                                                                      "origin"})).getMSTR();
        querier = new EventDCQuerier(getName(), getDNS(), config);
    }

    public boolean hasNext() {
        MicroSecondDate queryEnd = getEventTimeRange().getEndTime();
        MicroSecondDate quitDate = queryEnd.add(lag);
        logger.debug("Checking if more queries to the event server are in order.  The quit date is "
                + quitDate
                + " the last query was for "
                + getQueryStart()
                + " and we're querying to " + queryEnd);
        return quitDate.after(ClockUtil.now())
                || !getQueryStart().equals(queryEnd);
    }

    public CacheEvent[] next() {
        CacheEvent[] results = null;
        int retryCount = 0;
        while(results == null) {
            try {
                results = loadMoreResults();
            } catch(SystemException se) {
                if(retryCount++ > 0) {//Do nothing first time
                    GlobalExceptionHandler.handle("This exception was thrown by the event server.  I'm resetting the connection to the server and waiting "
                                                          + (sleepForCount(retryCount) / 1000)
                                                          + " seconds for things to calm down so that hopefully the next query will be successful.",
                                                  se);
                }
                querier.getEventDC().reset();
                try {
                    Thread.sleep(sleepForCount(retryCount));
                } catch(InterruptedException e) {
                    logger.debug("Query retry sleep interrupted", e);
                }
            }
        }
        return results;
    }
    
    protected CacheEvent[] loadMoreResults() {
        MicroSecondTimeRange queryTime = getQueryTime();
        CacheEvent[] results = querier.query(queryTime);
        updateQueryEdge(queryTime);
        return results;
    }
    
    protected void updateQueryEdge(MicroSecondTimeRange queryTime) {
        setQueryEdge(queryTime.getEndTime());
    }

    public static long sleepForCount(int count) {
        return (long)(Math.pow(10, count)) * 1000;
    }

    public TimeInterval getWaitBeforeNext() {
        if(caughtUpWithRealtime() && hasNext()) {
            resetQueryTimeForLag();
            return refreshInterval;
        }
        return new TimeInterval(0, UnitImpl.SECOND);
    }

    protected boolean caughtUpWithRealtime() {
        return ClockUtil.now()
                .subtract(getQueryStart())
                .lessThan(refreshInterval)
                || getQueryStart().equals(eventTimeRange.getEndTime());
    }

    public MicroSecondTimeRange getEventTimeRange() {
        return eventTimeRange;
    }

    /**
     * @return - the next time to start asking for events
     */
    protected MicroSecondDate getQueryStart() {
        try {
            return getQueryEdge();
        } catch(edu.sc.seis.fissuresUtil.database.NotFound e) {
            logger.debug("the query times database didn't have an entry for our server/dns combo, just use the time in the config file");
            return getEventTimeRange().getBeginTime();
        }
    }

    /**
     * @return - the next time range to be queried for events
     */
    protected MicroSecondTimeRange getQueryTime() {
        MicroSecondDate queryStart = getQueryStart();
        MicroSecondDate queryEnd = queryStart.add(increment);
        if(getEventTimeRange().getEndTime().before(queryEnd)) {
            queryEnd = getEventTimeRange().getEndTime();
        }
        if(ClockUtil.now().before(queryEnd)) {
            queryEnd = ClockUtil.now();
        }
        return new MicroSecondTimeRange(queryStart, queryEnd);
    }

    /**
     * Scoots the query time back by the event lag amount from the run
     * properties to the query start time at the earliest
     */
    private void resetQueryTimeForLag() {
        MicroSecondDate newEdge = getQueryStart().subtract(lag);
        if(newEdge.before(getEventTimeRange().getBeginTime())) {
            setQueryEdge(getEventTimeRange().getBeginTime());
        } else {
            setQueryEdge(newEdge);
        }
    }

    /**
     * @return - latest time queried
     */
    protected MicroSecondDate getQueryEdge() throws NotFound {
        try {
            return queryTimes.getQuery(getName(), getDNS());
        } catch(SQLException e) {
            throw new RuntimeException("Database trouble with EventFinder query table",
                                       e);
        }
    }

    /**
     * sets the latest time queried
     */
    protected void setQueryEdge(MicroSecondDate edge) {
        try {
            queryTimes.setQuery(getName(), getDNS(), edge);
        } catch(SQLException e) {
            throw new RuntimeException("Database trouble with EventFinder query table",
                                       e);
        }
    }

    private EventDCQuerier querier;

    private MicroSecondTimeRange eventTimeRange;

    private static Logger logger = Logger.getLogger(EventFinder.class);

    private JDBCQueryTime queryTimes;

    protected TimeInterval increment, lag, refreshInterval;
}// EventFinder
