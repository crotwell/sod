package edu.sc.seis.sod.hibernate;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.AbstractHibernateDB;
import edu.sc.seis.fissuresUtil.hibernate.HibernateUtil;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.LocalSeismogramWaveformWorkUnit;
import edu.sc.seis.sod.MotionVectorWaveformWorkUnit;
import edu.sc.seis.sod.QueryTime;
import edu.sc.seis.sod.RetryMotionVectorWaveformWorkUnit;
import edu.sc.seis.sod.RetryWaveformWorkUnit;
import edu.sc.seis.sod.RunProperties;
import edu.sc.seis.sod.SodConfig;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.Version;
import edu.sc.seis.sod.database.event.StatefulEvent;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;

public class SodDB extends AbstractHibernateDB {

	public SodDB() {
		this(HibernateUtil.getSessionFactory());
	}

	public SodDB(SessionFactory factory) {
		super(factory);
	}

	public EventChannelPair[] getSuspendedEventChannelPairs(
			String processingRule) throws SQLException {
		Status eventStationInit = Status.get(Stage.EVENT_STATION_SUBSETTER,
				Standing.INIT);
		Status processorSuccess = Status.get(Stage.PROCESSOR, Standing.SUCCESS);
		Stage[] stages = { Stage.EVENT_STATION_SUBSETTER,
				Stage.EVENT_CHANNEL_SUBSETTER, Stage.REQUEST_SUBSETTER,
				Stage.AVAILABLE_DATA_SUBSETTER, Stage.DATA_RETRIEVAL,
				Stage.PROCESSOR };
		Standing[] standings = { Standing.IN_PROG, Standing.INIT,
				Standing.SUCCESS };
		String query = "FROM edu.sc.seis.sod.EventChannelPair e WHERE e.StatusAsShort in ( ";
		for (int i = 0; i < stages.length; i++) {
			for (int j = 0; j < standings.length; j++) {
				Status curStatus = Status.get(stages[i], standings[j]);
				if (!curStatus.equals(processorSuccess)) {
					query += curStatus.getAsShort();
					query += ", ";
				}
			}
		}
		// get rid of last comma
		query = query.substring(0, query.length() - 2);
		query += ')';
		List out = getSession().createQuery(query).list();
		if (processingRule.equals(RunProperties.AT_LEAST_ONCE)) {
			return (EventChannelPair[]) out.toArray(new EventChannelPair[0]);
		} else {
			ArrayList reprocess = new ArrayList();
			Iterator it = out.iterator();
			while (it.hasNext()) {
				EventChannelPair pair = (EventChannelPair) it.next();
				Status curStatus = pair.getStatus();
				Stage currentStage = curStatus.getStage();
				if (!curStatus.equals(eventStationInit)) {
					pair.update(Status.get(currentStage,
							Standing.SYSTEM_FAILURE));
				} else {
					reprocess.add(pair);
				}
			}
			getSession().flush();
			return (EventChannelPair[]) reprocess
					.toArray(new EventChannelPair[0]);
		}
	}

	static String getNextEventStr = "From "
			+ StatefulEvent.class.getName()
			+ " e WHERE StatusAsShort = :popInProgress"
			+ Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG)
					.getAsShort();

	public EventChannelPair put(EventChannelPair eventChannelPair) {
		Session session = getSession();
		session.lock(eventChannelPair.getChannel(), LockMode.NONE);
		session.lock(eventChannelPair.getEvent(), LockMode.NONE);
		session.save(eventChannelPair);
		return eventChannelPair;
	}

	public EventVectorPair put(EventVectorPair eventVectorPair) {
		Session session = getSession();
		session.lock(eventVectorPair.getEvent(), LockMode.NONE);
		Channel[] chan = eventVectorPair.getChannelGroup().getChannels();
		for (int i = 0; i < chan.length; i++) {
			session.lock(chan[i], LockMode.NONE);
		}
		session.save(eventVectorPair);
		return eventVectorPair;
	}

	public EventVectorPair getEventVectorPair(EventChannelPair ecp) {
		String q = "from " + EventVectorPair.class.getName()
				+ " where ecp1 = :ecp or ecp2 = :ecp or ecp3 = :ecp";
		Session session = getSession();
		Query query = session.createQuery(q);
		query.setEntity(":ecp", ecp);
		query.setMaxResults(1);
		List result = query.list();
		if (result.size() > 0) {
			EventVectorPair out = (EventVectorPair) result.get(0);
			return out;
		}
		return null;
	}

	public void retry(LocalSeismogramWaveformWorkUnit unit) {
		if (unit instanceof RetryWaveformWorkUnit) {
			retry((RetryWaveformWorkUnit) unit);
			return;
		}
		RetryWaveformWorkUnit retry = new RetryWaveformWorkUnit(unit.getEcp());
		getSession().save(retry);
		flush();
	}

	public void retry(MotionVectorWaveformWorkUnit unit) {
		if (unit instanceof RetryMotionVectorWaveformWorkUnit) {
			retry((RetryMotionVectorWaveformWorkUnit) unit);
			return;
		}
		RetryMotionVectorWaveformWorkUnit retry = new RetryMotionVectorWaveformWorkUnit(
				unit.getEvp());
		Serializable dbid = getSession().save(retry);
		logger.debug("retry a new unit: dbid=" + retry.getDbid() + "  " + dbid);
		flush();
	}

	public void retry(RetryWaveformWorkUnit unit) {
		try {
			if (unit.getNumRetries() < maxRetries
					&& ClockUtil.now().subtract(
							new MicroSecondDate(unit.getEcp().getEvent()
									.get_preferred_origin().origin_time))
							.getValue(UnitImpl.SECOND) < seismogramLatency) {
				logger.debug("retry a retry: dbid=" + unit.getDbid());
				unit.updateRetries();
				getSession().update(unit);
			} else {
				// already retried too many times
				getSession().delete(unit);
			}
		} catch (NoPreferredOrigin e) {
			// should never happen
			throw new RuntimeException("Event has no preferred origin: " + e, e);
		}
	}

	public void retry(RetryMotionVectorWaveformWorkUnit unit) {
		try {
			if (unit.getNumRetries() < maxRetries
					&& ClockUtil.now().subtract(
							new MicroSecondDate(unit.getEvp().getEvent().get_preferred_origin().origin_time)).getValue(
							UnitImpl.SECOND) < seismogramLatency) {
				unit.updateRetries();
				getSession().update(unit);
			} else {
				// already retried too many times
				getSession().delete(unit);
			}
		} catch (NoPreferredOrigin e) {
			// should never happen
			throw new RuntimeException("Event has no preferred origin: "+e, e);
		}
	}

	/*
	 * 
	 * RunProperties runProps = Start.getRunProps(); SERVER_RETRY_DELAY =
	 * runProps.getServerRetryDelay(); sodDb = new SodDB(); retries = new
	 * JDBCRetryQueue("waveform"); retries.setMaxRetries(5); int
	 * minRetriesOnAvailableData = 3;
	 * retries.setMinRetries(minRetriesOnAvailableData);
	 * retries.setMinRetryWait((TimeInterval)runProps.getMaxRetryDelay()
	 * .divideBy(minRetriesOnAvailableData));
	 * retries.setEventDataLag(runProps.getSeismogramLatency()); corbaFailures =
	 * new JDBCRetryQueue("corbaFailure"); corbaFailures.setMinRetryWait(new
	 * TimeInterval(2, UnitImpl.HOUR)); corbaFailures.setMaxRetries(10);
	 */
	float minRetryDelay = (float) new TimeInterval(2, UnitImpl.HOUR)
			.getValue(UnitImpl.SECOND);
	float maxRetryDelay = (float) ((TimeInterval) Start.getRunProps()
			.getMaxRetryDelay()).getValue(UnitImpl.SECOND);
	float seismogramLatency = (float) ((TimeInterval) Start.getRunProps()
			.getSeismogramLatency()).getValue(UnitImpl.SECOND);
	int maxRetries = 5;
	float retryBase = 2;

	public List getAllRetries() {
		String q = "from edu.sc.seis.sod.RetryWaveformWorkUnit r ";
		Query query = getSession().createQuery(q);
		return query.list();
	}

	public RetryWaveformWorkUnit[] getRetryWaveformWorkUnits(int limit) {
		String q = "from edu.sc.seis.sod.RetryWaveformWorkUnit r where datediff('ss',:now, r.lastQuery) > :minDelay and (datediff('ss',:now, r.lastQuery) > :maxDelay or datediff('ss',:now, r.lastQuery) > power(:base, numRetries))  order by r.lastQuery desc";
		Query query = getSession().createQuery(q);
		query.setTimestamp("now", ClockUtil.now());
		query.setFloat("base", retryBase);
		query.setFloat("minDelay", minRetryDelay);
		query.setFloat("maxDelay", maxRetryDelay);
		query.setMaxResults(limit);
		List result = query.list();
		if (result.size() != 0) {
			logger.debug("Got " + result.size() + " retries");
		}
		RetryWaveformWorkUnit[] out = (RetryWaveformWorkUnit[]) result
				.toArray(new RetryWaveformWorkUnit[0]);
		for (int i = 0; i < out.length; i++) {
			out[i].getEcp().update(
					Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.INIT));
		}
		return out;
	}
    
    public int getNumSuccessful(CacheEvent event) throws SQLException {
        Query query = getSession().createQuery(successPerEvent);
        query.setEntity(":event", event);
        query.setMaxResults(1);
        List result = query.list();
        return ((Integer) result.get(0)).intValue();
    }
    
    public int getNumSuccessful(StationImpl station) throws SQLException {
        Query query = getSession().createQuery(success);
        query.setEntity(":sta", station);
        query.setMaxResults(1);
        List result = query.list();
        return ((Integer) result.get(0)).intValue();
    }

    public int getNumFailed(StationImpl station) throws SQLException {
		Query query = getSession().createQuery(failed);
		query.setEntity(":sta", station);
		query.setMaxResults(1);
		List result = query.list();
		return ((Integer) result.get(0)).intValue();
    }

    public int getNumRetry(StationImpl station) throws SQLException {
		Query query = getSession().createQuery(retry);
		query.setEntity(":sta", station);
		query.setMaxResults(1);
		List result = query.list();
		return ((Integer) result.get(0)).intValue();
    }
    
    public List getStationsForEvent(CacheEvent event) {
        String q = "select distinct ecp.channel.site.station from "+EventChannelPair.class.getName()+" ecp where ecp.event = :event";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
        
    }

	public int putConfig(SodConfig sodConfig) {
		Integer dbid = (Integer) getSession().save(sodConfig);
		return dbid.intValue();
	}

	public SodConfig getCurrentConfig() {
		String q = "From edu.sc.seis.sod.SodConfig c ORDER BY c.time desc";
		Query query = getSession().createQuery(q);
		query.setMaxResults(1);
		List result = query.list();
		if (result.size() > 0) {
			SodConfig out = (SodConfig) result.get(0);
			return out;
		}
		return null;
	}

	public QueryTime getQueryTime(String serverName, String serverDNS) {
		String q = "From edu.sc.seis.sod.QueryTime q WHERE q.serverName = :serverName AND q.serverDNS = :serverDNS";
		Query query = getSession().createQuery(q);
		query.setString("serverName", serverName);
		query.setString("serverDNS", serverDNS);
		List result = query.list();
		if (result.size() > 0) {
			QueryTime out = (QueryTime) result.get(0);
			return out;
		}
		return null;
	}

	public int putQueryTime(QueryTime qtime) {
		Integer dbid = (Integer) getSession().save(qtime);
		return dbid.intValue();
	}

	public Version getDBVersion() {
		String q = "From edu.sc.seis.sod.Version ORDER BY dbid desc";
		Session session = getSession();
		Query query = getSession().createQuery(q);
		query.setMaxResults(1);
		List result = query.list();
		if (result.size() > 0) {
			Version out = (Version) result.get(0);
			return out;
		}
		Version v = Version.current();
		session.save(v);
		return v;
	}

	protected Version putDBVersion() {
		Version v = getDBVersion();
		Version current = Version.current();
		current.setDbid(v.getDbid());
		getSession().merge(current);
		commit();
		return current;
	}

    public static String getRetryStatusRequest() {
        return getStatusRequest(RETRY_STATUS);
    }

    public static String getFailedStatusRequest() {
        return getStatusRequest(FAILED_STATUS);
    }

    public static String getStatusRequest(Status[] statii) {
        String request = "( " + statii[0].getAsShort();
        for(int i = 1; i < statii.length; i++) {
            request += ", " + statii[i].getAsShort();
        }
        request += ")";
        return request;
    }

    public static final Status[] FAILED_STATUS = new Status[] {Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.REQUEST_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.REQUEST_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.DATA_RETRIEVAL,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.DATA_RETRIEVAL,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.PROCESSOR,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.PROCESSOR,
                                                                          Standing.REJECT)};

    public static final Status[] RETRY_STATUS = new Status[] {Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                         Standing.RETRY),
                                                              Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                         Standing.CORBA_FAILURE),
                                                              Status.get(Stage.DATA_RETRIEVAL,
                                                                         Standing.CORBA_FAILURE),
                                                              Status.get(Stage.PROCESSOR,
                                                                         Standing.CORBA_FAILURE)};
    
    private static String retry, failed, success, successPerEvent;

    static{
            String baseStatement = "SELECT COUNT(*) FROM edu.sc.seis.sod.EventChannelPair ecp WHERE " +
                "ecp.channel.site.station = :sta " ;
            int pass = Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
            success = baseStatement + " AND status = " + pass;
            String failReq = getFailedStatusRequest();
            failed = baseStatement + " AND statusAsShort in " + failReq;
            String retryReq = getRetryStatusRequest();
            retry = baseStatement + " AND statusAsShort in " + retryReq;
             successPerEvent = "SELECT COUNT(*) FROM edu.sc.seis.sod.EventChannelPair ecp WHERE " +
            "ecp.event = :event  AND status = " + Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
    }
    
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(SodDB.class);

}