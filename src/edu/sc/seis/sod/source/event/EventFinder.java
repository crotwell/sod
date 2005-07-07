package edu.sc.seis.sod.source.event;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
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
        MicroSecondTimeRange queryTime = getQueryTime();
        CacheEvent[] results = querier.query(queryTime.getFissuresTimeRange());
        setQueryEdge(queryTime.getEndTime());
        return results;
    }

    public TimeInterval getWaitBeforeNext() {
        if(caughtUpWithRealtime() && hasNext()) {
            resetQueryTimeForLag();
            return refreshInterval;
        }
        return new TimeInterval(0, UnitImpl.SECOND);
    }

    private boolean caughtUpWithRealtime() {
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
    private MicroSecondDate getQueryStart() {
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
    private MicroSecondTimeRange getQueryTime() {
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
    private MicroSecondDate getQueryEdge() throws NotFound {
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
    private void setQueryEdge(MicroSecondDate edge) {
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

    private TimeInterval increment, lag, refreshInterval;
}// EventFinder
