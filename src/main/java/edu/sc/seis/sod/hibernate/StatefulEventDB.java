package edu.sc.seis.sod.hibernate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;


public class StatefulEventDB {

    protected StatefulEventDB() {
        trans = new EventToStatefulDBTranslater();
    }

    public long put(StatefulEvent event) {
        return trans.put(event);
    }
    
    public List<StatefulEvent> getAll() {
        ArrayList<StatefulEvent> out = new ArrayList<StatefulEvent>();
        List<CacheEvent> l = trans.getAll();
        for(CacheEvent e : l) {
            out.add((StatefulEvent)e);
        }
        return out;
    }
    
    public List<StatefulEvent> getAll(Status status) {
        String q = "from "+StatefulEvent.class.getName()+" e where e.status.stageInt = "+status.getStageInt()+" and e.status.standingInt = "+status.getStandingInt();
        Query query = trans.getSession().createQuery(q);
        return query.list();
    }
    
    public int getNumEventsOfStatus(Standing standing) {
        String q = "select count(*) from " + StatefulEvent.class.getName()
        + " e where e.status.standingInt = "+standing.getVal();
        Query query = getSession().createQuery(q);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            return ((Number)result.get(0)).intValue();
        }
        return 0;
    }

    public StatefulEvent getEvent(int dbid) throws NotFound {
        return (StatefulEvent)trans.getEvent(dbid);
    }

    public List<StatefulEvent> getEventInTimeRange(TimeRange range) {
        return getEventInTimeRange(range, Status.getFromShort((short)2310));
    }
     
    public List<StatefulEvent> getEventInTimeRange(TimeRange range, Status status) {
        String q = "from "+StatefulEvent.class.getName()+" e where ";
        if (status != null) { q += " e.status.stageInt = "+status.getStageInt()+" and e.status.standingInt = "+status.getStandingInt()+" AND ";}
        q += " e.preferred.originTime.time between :minTime AND :maxTime  ";
        Query query = trans.getSession().createQuery(q);

        query.setTimestamp("minTime", range.getBeginTime().getTimestamp());
        query.setTimestamp("maxTime", range.getEndTime().getTimestamp());
        return query.list();
    }
     
    public List<StatefulEvent> getEventInTimeRangeRegardlessOfStatus(TimeRange range) {
        return getEventInTimeRange(range, (Status)null);
    }

    public StatefulEvent getLastEvent() throws NotFound {
        return (StatefulEvent)trans.getLastEvent();
    }
    
    public StatefulEvent[] getEventsByTimeAndDepthRanges(Instant minTime,
                                                         Instant maxTime,
                                                      double minDepth,
                                                      double maxDepth) {
        CacheEvent[] ans = trans.getEventsByTimeAndDepthRanges(minTime, maxTime, minDepth, maxDepth);
        StatefulEvent[] out = new StatefulEvent[ans.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = (StatefulEvent)ans[i];
        }
        return out;
    }
    
    public int getNumWaiting() {
        Status status = Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.INIT);
        String q = "select count(*) from "
                + StatefulEvent.class.getName()
                + " e where e.status.stageInt = "+status.getStageInt()
                + " and e.status.standingInt = "+status.getStandingInt();
        Session session = trans.getSession();
        Query query = session.createQuery(q);
        List result = query.list();
        if(result.size() > 0) {
            return ((Long)result.get(0)).intValue();
        }
        return 0;
    }
    
    /** next successful event to process. Returns null if no more events. */
    public StatefulEvent getNext(Standing standing) {
        Status status = Status.get(Stage.EVENT_CHANNEL_POPULATION, standing);
        String q = "from "
                + StatefulEvent.class.getName()
                + " e where e.status.stageInt = "+status.getStageInt()+" and e.status.standingInt = "+status.getStandingInt();
        Session session = trans.getSession();
        Query query = session.createQuery(q);
        List result = query.list();
        if(result.size() > 0) {
            return (StatefulEvent)result.get(0);
        }
        return null;
    }

    public StatefulEvent getIdenticalEvent(CacheEvent e) {
        return (StatefulEvent)trans.getIdenticalEvent(e);
    }
    
    public List get(String statii, String order, boolean ascending) {
    	Query q = trans.getSession().createQuery("from "+StatefulEvent.class.getName()+" e "
    			+" where e.status in :statii order by :order :direction");
    	q.setString("statii", statii);
    	q.setString("order", order);
    	q.setString("direction", ascending ? "asc" : "desc");
    	return q.list();
    }

    public void flush() {
        trans.flush();
    }

    public void commit() {
        trans.commit();
    }

    public void rollback() {
        trans.rollback();
    }

    public Session getSession() {
        return trans.getSession();
    }
    
    EventToStatefulDBTranslater trans;

    public void restartCompletedEvents() {
        Status success = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                    Standing.SUCCESS);
        Status inProg = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                   Standing.IN_PROG);
        String q = "update " + StatefulEvent.class.getName()
                + " e set e.status.standingInt = :inProg where e.status.stageInt = "+success.getStageInt()
                +" and e.status.standingInt = "+success.getStandingInt();
        Query query = trans.getSession().createQuery(q);
        query.setShort("inProg", inProg.getAsShort());
        int updates = query.executeUpdate();
        logger.info("Reopen " + updates + " events");
    }
    
    public static final String TIME_ORDER = "preferred.originTime.time";
    
    private static StatefulEventDB singleton;
    
    public static StatefulEventDB getSingleton() {
        if (singleton == null) {
            singleton = new StatefulEventDB();
        }
        return singleton;
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StatefulEventDB.class);

    public CacheEvent[] getByName(String name) {
        String q = baseSuccessfulQuery+" AND " + "e.attr.name = :name";
        Query query = trans.getSession().createQuery(q);
        query.setString("name", name);
        List result = query.list();
        CacheEvent[] out = (CacheEvent[]) result.toArray(new CacheEvent[0]);
        return out;
    }
    

    private static Status success = Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                Standing.SUCCESS);
    
    private static String baseSuccessfulQuery = "from "+StatefulEvent.class.getName()+" e where " +
     " ( e.status.stageInt = "+success.getStageInt()+" and e.status.standingInt = "+success.getStandingInt()+" ) ";

    public String[] getCatalogs() {
        return trans.getCatalogs();
    }
    
    public String[] getContributors() {
        return trans.getContributors();
    }

    public String[] getCatalogsFor(String contributor) {
        return trans.getCatalogsFor(contributor);
    }
}

class EventToStatefulDBTranslater extends EventDB {

    protected Class getEventClass() {
        return StatefulEvent.class;
    }
}
