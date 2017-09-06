package edu.sc.seis.sod.source.event;

import java.time.Duration;
import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.QueryTime;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.NotFound;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.sod.source.network.AbstractNetworkSource;
import edu.sc.seis.sod.util.time.ClockUtil;


public abstract class AbstractEventSource extends AbstractSource implements EventSource {

    public AbstractEventSource(String name, int retries) {
        super(name, retries);
        refreshInterval = Start.getRunProps().getEventRefreshInterval();
        lag = Start.getRunProps().getEventLag();
        increment = Start.getRunProps().getEventQueryIncrement();
    }

    public AbstractEventSource(Element config, String defaultName) throws ConfigurationException {
        super(config, defaultName, -1);
        if(DOMHelper.hasElement(config, AbstractNetworkSource.REFRESH_ELEMENT)) {
            refreshInterval = SodUtil.loadTimeInterval(SodUtil.getElement(config, AbstractNetworkSource.REFRESH_ELEMENT));
        } else {
            refreshInterval = Start.getRunProps().getEventRefreshInterval();
        }
        if(DOMHelper.hasElement(config, AbstractEventSource.EVENT_QUERY_INCREMENT)) {
            increment = SodUtil.loadTimeInterval(SodUtil.getElement(config, AbstractEventSource.EVENT_QUERY_INCREMENT));
        } else {
            increment = Start.getRunProps().getEventQueryIncrement();
        }
        if(DOMHelper.hasElement(config, AbstractEventSource.EVENT_LAG)) {
            lag = SodUtil.loadTimeInterval(SodUtil.getElement(config, AbstractEventSource.EVENT_LAG));
        } else {
            lag = Start.getRunProps().getEventLag();
        }
    }

    @Override
    public Duration getWaitBeforeNext() {
        Instant now = ClockUtil.now();
        if (! caughtUpWithRealtime()) {
            return Duration.ofSeconds(0);
        }
        if (lastQueryTime == null) {
            // on null, make time old enough to force a query
            lastQueryTime = now.minus(refreshInterval).minus(nearRealTimeInterval);
        }
        Duration sleepTime = Duration.between(lastQueryTime, now).plus(refreshInterval);
        if (sleepTime.toNanos() < 0) {
            caughtUpToRealtime = false;
        }
        logger.debug("getWaitBeforeNext() lq="+lastQueryTime
                     +" sleep="+TimeUtils.durationToDoubleSeconds(sleepTime)
                     +"  now="+now+"  refesh="+TimeUtils.durationToDoubleSeconds(refreshInterval));
        return sleepTime;
    }
    


    protected boolean caughtUpWithRealtime() {
        return caughtUpToRealtime;
    }
    
    protected boolean isEverCaughtUpToRealtime() {
        return everCaughtUpToRealtime;
    }
    
    /**
     * @return - the next time to start asking for events
     */
    protected Instant getQueryStart() {
        try {
            return getQueryEdge();
        } catch (NotFound e) {
            logger.debug("the query times database didn't have an entry for our server/dns combo, just use the time in the config file");
            setQueryEdge(getEventTimeRange().getBeginTime());
            return getEventTimeRange().getBeginTime();
        }
    }

    /**
     * @return - the next time range to be queried for events
     */
    protected TimeRange getQueryTime() {
        Instant now = ClockUtil.now();
        Instant queryStart = getQueryStart();
        if (caughtUpWithRealtime()) {
            // have caught up with real time, so go back by lag
            queryStart = resetQueryTimeForLag();
            caughtUpToRealtime = false;
        }
        Instant queryEnd = queryStart.plus(increment);
        if (getEventTimeRange().getEndTime().isBefore(queryEnd)) {
            queryEnd = getEventTimeRange().getEndTime();
            logger.debug("Caught up with edge of event time range.");
            caughtUpToRealtime = true;
            everCaughtUpToRealtime = true;
        }
        if (now.isBefore(queryEnd)) {
            logger.info("Caught up with now.");
            queryEnd = now;
            caughtUpToRealtime = true;
            everCaughtUpToRealtime = true;
        }
        if (queryStart.isAfter(ClockUtil.wayFuture())) {
            throw new RuntimeException("start way in future: qs="+queryStart+" lag="+getLag()+" end="+queryEnd);
        }
        if (Duration.between(queryStart, queryEnd).toNanos() < TimeUtils.ONE_MINUTE.toNanos()) {
            logger.warn("Query for very short time window: start:"+queryStart+" end:"+queryEnd+" inc:"+increment+" now:"+now+"  cuwrt:"+caughtUpToRealtime+" ecuwrt:"+everCaughtUpToRealtime+"  tot end:"+getEventTimeRange().getEndTime());
        }
        return new TimeRange(queryStart, queryEnd);
    }
    
    public void increaseQueryTimeWidth() {
        increment = increment.multipliedBy(2);
    }
    
    /** decrease the time increment for queries, but only if it is larger than the minimum = 1Day 
     * to avoid many tiny queries to the server. */
    public void decreaseQueryTimeWidth() {
        if (getIncrement().toNanos() > MIN_INCREMENT.toNanos()) {
            increment = Duration.ofNanos(Math.round(.75*increment.toNanos()));
        }
    }

    /**
     * Scoots the query time back by the event lag amount from the run
     * properties to the query start time at the earliest
     */
    protected Instant resetQueryTimeForLag() {
        Instant newEdge = getQueryStart().minus(lag);
        if (newEdge.isBefore(getEventTimeRange().getBeginTime())) {
            newEdge = getEventTimeRange().getBeginTime();
        }
        return newEdge;
    }

    /**
     * @return - latest time queried
     */
    protected Instant getQueryEdge() throws NotFound {
        SodDB sdb = SodDB.getSingleton();
        QueryTime t = sdb.getQueryTime(getName());
        SodDB.commit();
        if (t == null) {throw new NotFound();}
        return t.getTime();
    }

    /**
     * sets the latest time queried
     */
    protected void setQueryEdge(Instant edge) {
        lastQueryTime = ClockUtil.now();
        SodDB sdb = SodDB.getSingleton();
        QueryTime qt = sdb.getQueryTime(getName());
        if (qt != null) {
            qt.setTime( edge);
            SodDB.getSession().saveOrUpdate(qt);
        } else {
            sdb.putQueryTime(new QueryTime(getName(), edge));
        }
        SodDB.commit();
    }

    protected void updateQueryEdge(TimeRange queryTime) {
        setQueryEdge(queryTime.getEndTime());
    }
    
    
    public Instant getSleepUntilTime() {
        return sleepUntilTime;
    }

    
    public Duration getLag() {
        return lag;
    }

    
    
    public Duration getIncrement() {
        return increment;
    }

    
    public void setIncrement(Duration increment) {
        this.increment = increment;
    }

    
    public void setLag(Duration lag) {
        this.lag = lag;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }
    
    protected boolean caughtUpToRealtime = false;
    
    protected boolean everCaughtUpToRealtime = false;
    
    protected Instant lastQueryTime = null;

    protected Instant sleepUntilTime = null;
    
    protected Duration increment, lag;

    protected Duration refreshInterval = Duration.ofMinutes(10);
    
    protected Duration nearRealTimeInterval = Duration.ofMinutes(2);
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractEventSource.class);

    public static final String EVENT_QUERY_INCREMENT = "eventQueryIncrement";
    
    public static final String EVENT_REFRESH_INTERVAL = "eventRefreshInterval";
    
    public static final String EVENT_LAG = "eventLag";

    public static final Duration MIN_INCREMENT = Duration.ofDays(1);
}
