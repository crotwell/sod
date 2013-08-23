package edu.sc.seis.sod.source.event;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.QueryTime;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.sod.source.network.AbstractNetworkSource;


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
    public TimeInterval getWaitBeforeNext() {
        if (sleepUntilTime != null) {
            logger.debug(getName()+" "+"sleeping caught up, "+refreshInterval+" "+sleepUntilTime);
            return sleepUntilTime.subtract(ClockUtil.now());
        }
        return new TimeInterval(0, UnitImpl.SECOND);
    }
    


    protected boolean caughtUpWithRealtime() {
        return ClockUtil.now().subtract(refreshInterval).before(getQueryStart())
                || getQueryStart().add(new TimeInterval(10, UnitImpl.SECOND)).after(getEventTimeRange().getEndTime());
    }
    
    /**
     * @return - the next time to start asking for events
     */
    protected MicroSecondDate getQueryStart() {
        try {
            return getQueryEdge();
        } catch (edu.sc.seis.fissuresUtil.database.NotFound e) {
            logger.debug("the query times database didn't have an entry for our server/dns combo, just use the time in the config file");
            setQueryEdge(getEventTimeRange().getBeginTime());
            return getEventTimeRange().getBeginTime();
        }
    }

    /**
     * @return - the next time range to be queried for events
     */
    protected MicroSecondTimeRange getQueryTime() {
        MicroSecondDate now = ClockUtil.now();
        MicroSecondDate queryStart = getQueryStart();
        MicroSecondDate queryEnd = queryStart.add(increment);
        if (getEventTimeRange().getEndTime().before(queryEnd)) {
            queryEnd = getEventTimeRange().getEndTime();
        }
        if (now.before(queryEnd)) {
            queryEnd = ClockUtil.now();
        }
        if (now.subtract(getLag()).before(queryStart)) {
            queryStart = now.subtract(getLag());
        }
        return new MicroSecondTimeRange(queryStart, queryEnd);
    }
    
    public void increaseQueryTimeWidth() {
        increment = (TimeInterval)increment.multiplyBy(2);
    }
    public void decreaseQueryTimeWidth() {
        increment = (TimeInterval)increment.multiplyBy(.75);
    }

    /**
     * Scoots the query time back by the event lag amount from the run
     * properties to the query start time at the earliest
     */
    protected void resetQueryTimeForLag() {
        MicroSecondDate newEdge = getQueryStart().subtract(lag);
        if (newEdge.before(getEventTimeRange().getBeginTime())) {
            setQueryEdge(getEventTimeRange().getBeginTime());
        } else {
            setQueryEdge(newEdge);
        }
    }

    /**
     * @return - latest time queried
     */
    protected MicroSecondDate getQueryEdge() throws NotFound {
        SodDB sdb = SodDB.getSingleton();
        QueryTime t = sdb.getQueryTime(getName(), NO_DNS);
        SodDB.commit();
        if (t == null) {throw new NotFound();}
        return new MicroSecondDate(t.getTime());
    }

    /**
     * sets the latest time queried
     */
    protected void setQueryEdge(MicroSecondDate edge) {
        SodDB sdb = SodDB.getSingleton();
        QueryTime qt = sdb.getQueryTime(getName(), NO_DNS);
        if (qt != null) {
            qt.setTime( edge.getTimestamp());
            SodDB.getSession().saveOrUpdate(qt);
        } else {
            sdb.putQueryTime(new QueryTime(getName(), NO_DNS, edge.getTimestamp()));
        }
        SodDB.commit();
    }

    protected void updateQueryEdge(MicroSecondTimeRange queryTime) {
        setQueryEdge(queryTime.getEndTime());
    }
    
    
    public MicroSecondDate getSleepUntilTime() {
        return sleepUntilTime;
    }

    
    public TimeInterval getLag() {
        return lag;
    }

    
    public TimeInterval getRefreshInterval() {
        return refreshInterval;
    }

    public static final String NO_DNS = "NO_DNS";

    protected MicroSecondDate sleepUntilTime = null;
    
    protected TimeInterval increment, lag;
    
    protected TimeInterval refreshInterval = new TimeInterval(10, UnitImpl.MINUTE);
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractEventSource.class);

    public static final String EVENT_QUERY_INCREMENT = "eventQueryIncrement";
    
    public static final String EVENT_REFRESH_INTERVAL = "eventRefreshInterval";
    
    public static final String EVENT_LAG = "eventLag";
}
