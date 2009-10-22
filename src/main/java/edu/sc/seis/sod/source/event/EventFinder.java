package edu.sc.seis.sod.source.event;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;
import org.w3c.dom.Element;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.RetryStrategy;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.QueryTime;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.subsetter.AbstractSource;

public class EventFinder extends AbstractSource implements EventSource {

	public EventFinder(Element config) throws Exception {
		super(config);
		eventFinderId = eventFinderCount++;
		processConfig(config);
		refreshInterval = Start.getRunProps().getEventRefreshInterval();
		lag = Start.getRunProps().getEventLag();
		increment = Start.getRunProps().getEventQueryIncrement();
	}

	protected void processConfig(Element config) throws ConfigurationException {
		Element queryTimeEl = DOMHelper.extractElement(config,
				"originTimeRange");
		if (queryTimeEl == null) {
			queryTimeEl = DOMHelper.extractElement(config, "networkTimeRange");
		}
		eventTimeRange = ((MicroSecondTimeRangeSupplier) SodUtil.load(queryTimeEl, new String[] {
				"eventArm", "origin" }));
		querier = new EventDCQuerier(getName(), getDNS(), getRetries(), config);
	}
    
    public String getDescription() {
        return "EventFinder Source: "+getDNS()+" "+getName();
    }
    
	public boolean hasNext() {
		MicroSecondDate queryEnd = getEventTimeRange().getEndTime();
		MicroSecondDate quitDate = queryEnd.add(lag);
		logger
				.debug("Checking if more queries to the event server are in order.  The quit date is "
						+ quitDate
						+ " the last query was for "
						+ getQueryStart()
						+ " and we're querying to "
						+ queryEnd);
		return  quitDate.after(ClockUtil.now())
				|| !getQueryStart().equals(queryEnd);
	}

	private CacheEvent[] internalNext() {
        MicroSecondTimeRange queryTime = getQueryTime();
	    CacheEvent[] results = querier.query(queryTime);
        logger.debug("Retrieved"+results.length+" events for time range "+queryTime);
        updateQueryEdge(queryTime);
        return results;
	}
	
	public CacheEvent[] next() {
        int count = 0;
        SystemException latest;
        try {
            return internalNext();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(retryStrat.shouldRetry(latest, querier.getEventDC(), count++)) {
            try {
                CacheEvent[]  result = internalNext();
                retryStrat.serverRecovered(querier.getEventDC());
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
	}

	protected void updateQueryEdge(MicroSecondTimeRange queryTime) {
		setQueryEdge(queryTime.getEndTime());
	}

	public TimeInterval getWaitBeforeNext() {
		if (caughtUpWithRealtime() && hasNext()) {
			resetQueryTimeForLag();
			return refreshInterval;
		}
		return new TimeInterval(0, UnitImpl.SECOND);
	}

	protected boolean caughtUpWithRealtime() {
		return ClockUtil.now().subtract(getQueryStart()).lessThan(
				refreshInterval)
				|| getQueryStart().equals(getEventTimeRange().getEndTime());
	}

	public MicroSecondTimeRange getEventTimeRange() {
		return eventTimeRange.getMSTR();
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
		MicroSecondDate queryStart = getQueryStart();
		MicroSecondDate queryEnd = queryStart.add(increment);
		if (getEventTimeRange().getEndTime().before(queryEnd)) {
			queryEnd = getEventTimeRange().getEndTime();
		}
		if (ClockUtil.now().before(queryEnd)) {
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
	    QueryTime t = sdb.getQueryTime(getUniqueName(), getDNS());
	    SodDB.commit();
	    if (t == null) {throw new NotFound();}
	    return new MicroSecondDate(t.getTime());
	}

	/**
	 * sets the latest time queried
	 */
	protected void setQueryEdge(MicroSecondDate edge) {
        SodDB sdb = SodDB.getSingleton();
	    QueryTime qt = sdb.getQueryTime(getUniqueName(), getDNS());
	    if (qt != null) {
	        qt.setTime( edge.getTimestamp());
	        SodDB.getSession().saveOrUpdate(qt);
	    } else {
	        sdb.putQueryTime(new QueryTime(getUniqueName(), getDNS(), edge.getTimestamp()));
	    }
	    SodDB.commit();
	}

	private String getUniqueName() {
		return getName() + eventFinderId;
	}

	// Unique among eventFinders and constant for this eventFinder for repeated
	// uses of the same recipe file
	private int eventFinderId;

	private static int eventFinderCount = 0;

	private EventDCQuerier querier;

	private MicroSecondTimeRangeSupplier eventTimeRange;

	private static Logger logger = Logger.getLogger(EventFinder.class);

	protected TimeInterval increment, lag, refreshInterval;

	private RetryStrategy retryStrat = Start.createRetryStrategy(getRetries());
}// EventFinder
