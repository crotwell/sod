package edu.sc.seis.sod.hibernate;

import java.time.Instant;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.event.OriginImpl;

public class EventDB extends AbstractHibernateDB {

    protected EventDB() {
		initQueryStrings();
	}

	protected void initQueryStrings() {
		getLastEventString = "From " + getEventClass().getName()
				+ " e ORDER BY e.id desc";
		finderQueryBase = "select distinct e FROM "
				+ getEventClass().getName()
				+ " e inner join e.preferred.magnitudeList m "
				+ "WHERE e.preferred.location.latitude between :minLat AND :maxLat "
				+ "AND m.value between :minMag AND :maxMag  "
				+ "AND e.preferred.originTime.time between :minTime AND :maxTime  "
				+ "AND e.preferred.location.depth.value between :minDepth and :maxDepth  ";
		finderQueryAvoidDateline = finderQueryBase
				+ "AND e.preferred.location.longitude between :minLon and :maxLon ";
		finderQueryAroundDateline = finderQueryBase
				+ " AND ((:minLon <= e.preferred.location.longitude) OR (e.preferred.location.longitude <= :maxLon))";
		getIdenticalEventString = "From " + getEventClass().getName()
				+ " e WHERE " + "e.preferred.originTime.time = :originTime "
				+ "AND e.preferred.location.latitude = :lat "
				+ "AND e.preferred.location.longitude = :lon "
                + "AND e.preferred.location.depth.value = :depth "
                + "AND e.preferred.catalog = :catalog "
                + "AND e.preferred._id = :originid ";
		eventByTimeAndDepth = "From " + getEventClass().getName()
        + " e WHERE " + "e.preferred.originTime.time between :minTime and :maxTime "
        + "AND e.preferred.location.depth.value between :minDepth and :maxDepth";
		eventByName = "From " + getEventClass().getName()
        + " e WHERE " + "e.attr.name = :name";
	}
	
	public List<CacheEvent> getAll() {
	    return getSession().createQuery("from "+getEventClass().getName()).list();
	}

	public CacheEvent[] getByName(String name) {
        Query query = getSession().createQuery(eventByName);
        query.setString("name", name);
        List result = query.list();
        CacheEvent[] out = (CacheEvent[]) result.toArray(new CacheEvent[0]);
        return out;
	}

	public CacheEvent getEvent(int dbid) throws NotFound {
		Session session = getSession();
		CacheEvent out = (CacheEvent) session.get(getEventClass(), new Integer(
				dbid));
		if (out == null) {
			throw new NotFound();
		}
		return out;
	}

	public long put(CacheEvent event) {
		Session session = getSession();
		internUnit(event);
		Integer dbid = (Integer) session.save(event);
		return dbid.longValue();
	}
	
	public void delete(CacheEvent event) {
	    getSession().delete(event);
	}

	public CacheEvent getLastEvent() throws NotFound {
		Session session = getSession();
		Query query = session.createQuery(getLastEventString);
		query.setMaxResults(1);
		List result = query.list();
		if (result.size() > 0) {
			CacheEvent out = (CacheEvent) result.get(0);
			return out;
		}
		throw new NotFound();
	}

	public CacheEvent getIdenticalEvent(CacheEvent e) {
		Session session = getSession();
		Query query = session.createQuery(getIdenticalEventString);
		query.setMaxResults(1);
		try {
            query.setString("catalog", e.get_preferred_origin().getCatalog());
            query.setString("originid", e.get_preferred_origin().get_id());
			query.setTimestamp("originTime", e
					.get_preferred_origin().getOriginTime());
			query.setDouble("depth",
					e.get_preferred_origin().getLocation().depth.getValue());
			query.setDouble("lat",
					e.get_preferred_origin().getLocation().latitude);
			query.setDouble("lon",
					e.get_preferred_origin().getLocation().longitude);
			List result = query.list();
			if (result.size() > 0) {
				CacheEvent out = (CacheEvent) result.get(0);
				return out;
			}
		} catch (NoPreferredOrigin npo) {

		}
		return null;
	}
	
	public String[] getCatalogs() {
	    Query q = getSession().createQuery("select distinct catalog from "+OriginImpl.class.getName());
	    List out = q.list();
	    return (String[])out.toArray(new String[0]);
	}
    
    public String[] getContributors() {
        Query q = getSession().createQuery("select distinct contributor from "+OriginImpl.class.getName());
        List out = q.list();
        return (String[])out.toArray(new String[0]);
    }
    
    public String[] getCatalogsFor(String contributor) {
        Query q = getSession().createQuery("select distinct catalog from "+OriginImpl.class.getName()+" where contributor = :contributor");
        q.setString("contributor", contributor);
        List out = q.list();
        return (String[])out.toArray(new String[0]);
    }

    
    public CacheEvent[] getEventsByTimeAndDepthRanges(Instant minTime,
                                                      Instant maxTime,
                                                      double minDepth,
                                                      double maxDepth) {
        Session session = getSession();
        Query query = session.createQuery(eventByTimeAndDepth);
        query.setTimestamp("minTime", minTime);
        query.setTimestamp("maxTime", maxTime);
        query.setDouble("minDepth", minDepth);
        query.setDouble("maxDepth", maxDepth);
        List result = query.list();
        CacheEvent[] out = (CacheEvent[]) result.toArray(new CacheEvent[0]);
        return out;
    }
    
	public static EventDB getSingleton() {
	    if (singleton == null) {
	        singleton = new EventDB();
	    }
	    return singleton;
	}
	
	/**
	 * override to use queries on subclasses of CacheEvent. For example SOD uses
	 * StatefulEvent.
	 */
	protected Class getEventClass() {
		return CacheEvent.class;
	}
	
	protected void internUnit(CacheEvent event) {
        internUnit(event.getOrigin().getLocation());
        OriginImpl[] origins = event.get_origins();
        for(int i = 0; i < origins.length; i++) {
            internUnit(origins[i].getLocation());
        }
	}

	protected String getLastEventString;

	protected String finderQueryBase;

	protected String finderQueryAvoidDateline;

	protected String finderQueryAroundDateline;

	protected String getIdenticalEventString;
	
	protected String eventByTimeAndDepth;
	
	protected String eventByName;

    private static EventDB singleton;
    
	public static final float INCONCEIVABLY_SMALL_MAGNITUDE = -99.0f;
	         
	public static final float INCONCEIVABLY_LARGE_MAGNITUDE = 12.0f;
	         
	public static final float INCONCEIVABLY_SMALL_DEPTH = -99.0f;
	         
	public static final float INCONCEIVABLY_LARGE_DEPTH = 7000.0f;
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventDB.class);
	
}
