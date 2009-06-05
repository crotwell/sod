package edu.sc.seis.sod.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.querier.EventFinderQuery;
import edu.sc.seis.fissuresUtil.hibernate.EventDB;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;

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

    public StatefulEvent getEvent(int dbid) throws NotFound {
        return (StatefulEvent)trans.getEvent(dbid);
    }

    public List<StatefulEvent> getEventInTimeRange(MicroSecondTimeRange range) {
        return getEventInTimeRange(range, Status.getFromShort((short)2310));
    }
     
    public List<StatefulEvent> getEventInTimeRange(MicroSecondTimeRange range, Status status) {
        String q = "from "+StatefulEvent.class.getName()+" e where ";
        if (status != null) { q += " e.status.stageInt = "+status.getStageInt()+" and e.status.standingInt = "+status.getStandingInt()+" AND ";}
        q += " e.preferred.originTime.time between :minTime AND :maxTime  ";
        Query query = trans.getSession().createQuery(q);

        query.setTimestamp("minTime", range.getBeginTime().getTimestamp());
        query.setTimestamp("maxTime", range.getEndTime().getTimestamp());
        return query.list();
    }

    public List<StatefulEvent> query(EventFinderQuery q) {
        List<CacheEvent> ans = trans.query(q);
        List<StatefulEvent> out = new ArrayList<StatefulEvent>(ans.size());
        for(CacheEvent e : ans) {
            out.add((StatefulEvent)e);
        }
        return out;
    }

    public StatefulEvent getLastEvent() throws NotFound {
        return (StatefulEvent)trans.getLastEvent();
    }
    
    public StatefulEvent[] getEventsByTimeAndDepthRanges(MicroSecondDate minTime,
                                                      MicroSecondDate maxTime,
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
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(StatefulEventDB.class);
}

class EventToStatefulDBTranslater extends EventDB {

    protected Class getEventClass() {
        return StatefulEvent.class;
    }
}
