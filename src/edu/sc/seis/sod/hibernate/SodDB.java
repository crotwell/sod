package edu.sc.seis.sod.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunctionTemplate;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.hibernate.AbstractHibernateDB;
import edu.sc.seis.sod.AbstractEventChannelPair;
import edu.sc.seis.sod.AbstractEventPair;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.QueryTime;
import edu.sc.seis.sod.RunProperties;
import edu.sc.seis.sod.SodConfig;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.Version;

public class SodDB extends AbstractHibernateDB {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SodDB.class);

    static String configFile = "edu/sc/seis/sod/hibernate/sod.hbm.xml";

    protected SodDB() {} // only for singleton
    
    public static void configHibernate(Configuration config) {
        logger.debug("adding to HibernateUtil   " + configFile);
        config.addResource(configFile, SodDB.class.getClassLoader());
        if(ConnMgr.getURL().startsWith("jdbc:hsql")) {
            config.addSqlFunction("datediff",
                                  new SQLFunctionTemplate(Hibernate.LONG,
                                                          "datediff(?1, ?2, ?3)"));
            config.addSqlFunction("milliseconds_between",
                                  new SQLFunctionTemplate(Hibernate.LONG,
                                                          "datediff('millisecond', ?1, ?2)"));
            config.addSqlFunction("seconds_between",
                                  new SQLFunctionTemplate(Hibernate.LONG,
                                                          "datediff('second', ?1, ?2)"));
        } else if(ConnMgr.getURL().startsWith("jdbc:postgresql")) {
            config.addSqlFunction("milliseconds_between",
                                  new SQLFunctionTemplate(Hibernate.LONG,
                                                          "extract(epoch from (?2 - ?1)) * 1000"));
            config.addSqlFunction("seconds_between",
                                  new SQLFunctionTemplate(Hibernate.LONG,
                                                          "extract(epoch from (?2 - ?1))"));
        }
    }

    public void reopenSuspendedEventChannelPairs(String processingRule, boolean vector) {
        Stage[] stages = {Stage.EVENT_STATION_SUBSETTER,
                          Stage.EVENT_CHANNEL_SUBSETTER,
                          Stage.REQUEST_SUBSETTER,
                          Stage.AVAILABLE_DATA_SUBSETTER,
                          Stage.DATA_RETRIEVAL,
                          Stage.PROCESSOR};
        String stageList = " ( ";
        for(int i = 0; i < stages.length; i++) {
            stageList += stages[i].getVal()+", ";
        }
        stageList = stageList.substring(0, stageList.length() - 2);
        stageList += " ) ";
        Standing[] standings = {Standing.IN_PROG,
                                Standing.INIT,
                                Standing.SUCCESS};
        String standingList = " ( ";
        for(int i = 0; i < standings.length; i++) {
            standingList += standings[i].getVal()+", ";
        }
        standingList = standingList.substring(0, standingList.length() - 2);
        standingList += " ) ";
        String query;
        String setStmt;
        if(processingRule.equals(RunProperties.AT_LEAST_ONCE)) {
            setStmt = " stageInt = "+Stage.EVENT_CHANNEL_SUBSETTER.getVal()+", standingInt = "+Standing.INIT.getVal();
        } else {
            setStmt = " standingInt = "+Standing.SYSTEM_FAILURE.getVal();
        }
        String queryEnd = " set "+setStmt
        +" WHERE status.stageInt in "+stageList+" AND status.standingInt in "+standingList
        +" AND NOT (status.stageInt = "+Stage.PROCESSOR.getVal()+" AND status.standingInt = "+Standing.SUCCESS.getVal()+" ) "
        +" AND NOT (status.stageInt = "+Stage.EVENT_STATION_SUBSETTER.getVal()+" AND status.standingInt = "+Standing.INIT.getVal()+" ) ";
        query = "UPDATE "+EventChannelPair.class.getName()+queryEnd;
        int out = getSession().createQuery(query).executeUpdate();
        query = "UPDATE "+EventVectorPair.class.getName()+queryEnd;
        out += getSession().createQuery(query).executeUpdate();
    }

    public EventNetworkPair put(EventNetworkPair eventNetworkPair) {
        logger.debug("Put "+eventNetworkPair);
        Session session = getSession();
        session.lock(eventNetworkPair.getNetwork(), LockMode.NONE);
        session.lock(eventNetworkPair.getEvent(), LockMode.NONE);
        session.saveOrUpdate(eventNetworkPair);
        synchronized(enpToDo) {
            enpToDo.offer(eventNetworkPair);
        }
        return eventNetworkPair;
    }

    public EventStationPair put(EventStationPair eventStationPair) {
        logger.debug("Put ("+eventStationPair.getEventDbId()+",s "+eventStationPair.getStationDbId()+") "+eventStationPair);
        Session session = getSession();
        session.lock(eventStationPair.getStation(), LockMode.NONE);
        session.lock(eventStationPair.getEvent(), LockMode.NONE);
        session.saveOrUpdate(eventStationPair);
        synchronized(espToDo) {
            espToDo.offer(eventStationPair);
        }
        return eventStationPair;
    }

    public EventChannelPair put(EventChannelPair eventChannelPair) {
        logger.debug("Put "+eventChannelPair);
        Session session = getSession();
        session.lock(eventChannelPair.getChannel(), LockMode.NONE);
        session.lock(eventChannelPair.getEvent(), LockMode.NONE);
        session.saveOrUpdate(eventChannelPair);
        return eventChannelPair;
    }
    
    public boolean isESPTodo() {
        synchronized(espToDo) {
            return ! espToDo.isEmpty();
        }
    }
    
    public boolean isENPTodo() {
        synchronized(enpToDo) {
            return ! enpToDo.isEmpty();
        }
    }

    /** next successful event-network to process from cache. 
     * Returns null if no more events in cache. */
    public synchronized EventNetworkPair getNextENPFromCache() {
        EventNetworkPair enp;
        synchronized(enpToDo) {
            enp = enpToDo.poll();
        }
        if (enp != null) {
            // might be new thread
            // ok to use even though might not be committed as hibernate flushes
            // due to native generator for id
            return (EventNetworkPair)getSession().get(EventNetworkPair.class, 
                                                      new Long(enp.getDbid()));
        }
        return null;
    }

    /** next successful event-network to process. Returns null if no more events. */
    public synchronized EventNetworkPair getNextENP() {
        if ( ! isENPTodo()) {
            populateENPToDo();
        }
        return getNextENPFromCache();
    }
    
    public synchronized void populateENPToDo() {
        String q = "from "
                + EventNetworkPair.class.getName()
                + " e "
                + " left join fetch e.event "
                + " left join fetch e.network "
                + " where e.status.stageInt = "+Stage.EVENT_CHANNEL_POPULATION.getVal()
                +" and e.status.standingInt = :standing";
        Query query = getSession().createQuery(q);
        query.setInteger("standing", Standing.INIT.getVal());
        query.setMaxResults(100);
        List<EventNetworkPair> result = query.list();
        for (EventNetworkPair enpResult : result) {
            synchronized(enpToDo) {
                enpToDo.offer(enpResult);
            }
        }
    }

    /** next successful event-station to process from memory cache. 
     * Returns null if no more esp in memory. */
    public synchronized EventStationPair getNextESPFromCache() {
        EventStationPair esp;
        synchronized(espToDo) {
            esp = espToDo.poll();
        }
        if (esp != null) {
            // might be new thread
            // ok to use even though might not be committed as hibernate flushes
            // due to native generator for id
            return (EventStationPair)getSession().get(EventStationPair.class, 
                                                      new Long(esp.getDbid()));
        }
        return null;
    }
    
    /** next successful event-station to process. Returns null if no more events. */
    public synchronized EventStationPair getNextESP() {
        if (! isESPTodo()) {
            populateESPToDo();
        }
        return getNextESPFromCache();
    }
    
    public synchronized void populateESPToDo() {
        String q = "from "
                + EventStationPair.class.getName()
                + " e "
                + " left join fetch e.event "
                + " left join fetch e.station "
                + " left join fetch e.station.networkAttr "
                + " where e.status.stageInt = "+Stage.EVENT_CHANNEL_POPULATION.getVal()
                + " and e.status.standingInt = :inProg ";
        Query query = getSession().createQuery(q);
        query.setInteger("inProg", Standing.INIT.getVal());
        query.setMaxResults(1000);
        List<EventStationPair> result = query.list();
        for (EventStationPair eventStationPair : result) {
            synchronized(espToDo) {
                espToDo.offer(eventStationPair);
            }
        }
    }

    /** next successful event-channel to process. Returns null if no more events. */
    public AbstractEventChannelPair getNextECP() {
        String q = "from "
                + AbstractEventChannelPair.class.getName()
                + " e "
                + " left join fetch e.event "
                + " where e.status.stageInt = "+Stage.EVENT_CHANNEL_POPULATION.getVal()
                + " and e.status.standingInt = :inProg";
        Query query = getSession().createQuery(q);
        query.setInteger("inProg", Standing.INIT.getVal());
        query.setMaxResults(1);
        List<AbstractEventChannelPair> result = query.list();
        if(result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public synchronized AbstractEventChannelPair getNextRetryECPFromCache() {
        AbstractEventChannelPair ecp;
        synchronized(retryToDo) {
            ecp = retryToDo.poll();
        }
        if (ecp != null) {
            return (AbstractEventChannelPair)getSession().get(ecp.getClass(), 
                                                             new Long(ecp.getDbid()));
        }
        return null;
    }

    public synchronized AbstractEventChannelPair getNextRetryECP() {
        if (! retryToDo.isEmpty()) {
            return getNextRetryECPFromCache();
        }
        String q = "from "
                + AbstractEventChannelPair.class.getName()
                + "  where (status.standingInt = "
                + Standing.RETRY.getVal()
                + " or status.standingInt = "
                + Standing.CORBA_FAILURE.getVal()
                + " )  and seconds_between(:now, lastQuery) > :minDelay "
                + " and numRetries < "+maxRetries
                +" and (seconds_between(:now, lastQuery) > :maxDelay or seconds_between(:now, lastQuery) > power(:base, numRetries))  order by numRetries";
        Query query = getSession().createQuery(q);
        query.setTimestamp("now", ClockUtil.now().getTimestamp());
        query.setFloat("base", retryBase);
        query.setFloat("minDelay", minRetryDelay);
        query.setFloat("maxDelay", maxRetryDelay);
        query.setMaxResults(1000);
        List<AbstractEventChannelPair> result = query.list();
        for (AbstractEventChannelPair abstractEventChannelPair : result) {
            synchronized(retryToDo) {
                retryToDo.offer(abstractEventChannelPair);
            }
        }
        return getNextRetryECPFromCache();
    }

    public int getNumWorkUnits(Standing standing) {
        return getNumWorkUnits(standing, AbstractEventPair.class);
    }

    public int getNumEventNetworkWorkUnits(Standing standing) {
        return getNumWorkUnits(standing, EventNetworkPair.class);
    }
    
    private int getNumWorkUnits(Standing standing, Class EventPairClass) {
        String q = "select count(*) from " + EventPairClass.getName()
        + " e where e.status.stageInt = "+Stage.EVENT_CHANNEL_POPULATION.getVal()
        + " and e.status.standingInt = "+standing.getVal()
        + " and e.numRetries =  0";
        Query query = getSession().createQuery(q);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            return ((Number)result.get(0)).intValue();
        }
        return 0;
    }

    public EventChannelPair getECP(CacheEvent event, ChannelImpl chan) {
        Query query = getSession().createQuery("from "
                + EventChannelPair.class.getName()
                + " where event = :event and channel = :channel");
        query.setEntity("event", event);
        query.setEntity("channel", chan);
        query.setMaxResults(1);
        List<EventChannelPair> result = query.list();
        if(result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public EventVectorPair put(EventVectorPair eventVectorPair) {
        Session session = getSession();
        session.lock(eventVectorPair.getEvent(), LockMode.NONE);
        Channel[] chan = eventVectorPair.getChannelGroup().getChannels();
        for(int i = 0; i < chan.length; i++) {
            session.lock(chan[i], LockMode.NONE);
        }
        session.saveOrUpdate(eventVectorPair);
        return eventVectorPair;
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
    float minRetryDelay = (float)new TimeInterval(2, UnitImpl.HOUR).getValue(UnitImpl.SECOND);

    float maxRetryDelay = (float)((TimeInterval)Start.getRunProps()
            .getMaxRetryDelay()).getValue(UnitImpl.SECOND);

    float seismogramLatency = (float)((TimeInterval)Start.getRunProps()
            .getSeismogramLatency()).getValue(UnitImpl.SECOND);

    int maxRetries = 5;

    float retryBase = 2;


    public int getNumSuccessful() {
        Query query = getSession().createQuery(COUNT + totalSuccess);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumSuccessful(CacheEvent event) {
        Query query = getSession().createQuery(COUNT + successPerEvent);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumSuccessful(StationImpl station) {
        Query query = getSession().createQuery(COUNT + success);
        query.setEntity("sta", station);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumSuccessful(CacheEvent event, StationImpl station) {
        Query query = getSession().createQuery(COUNT + successPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumFailed(StationImpl station) {
        Query query = getSession().createQuery(COUNT + failed);
        query.setEntity("sta", station);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumFailed(CacheEvent event, StationImpl station) {
        Query query = getSession().createQuery(COUNT + failedPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumFailed(CacheEvent event) {
        Query query = getSession().createQuery(COUNT + failedPerEvent);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumRetry(StationImpl station) {
        Query query = getSession().createQuery(COUNT + retry);
        query.setEntity("sta", station);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumRetry(CacheEvent event) {
        Query query = getSession().createQuery(COUNT + retryPerEvent);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumRetry(CacheEvent event, StationImpl station) {
        Query query = getSession().createQuery(COUNT + retryPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public List<AbstractEventChannelPair> getAll(CacheEvent event) {
        Query query = getSession().createQuery(eventBase);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getSuccessful(CacheEvent event) {
        Query query = getSession().createQuery(successPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getSuccessful(StationImpl station) {
        Query query = getSession().createQuery(success);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<AbstractEventChannelPair> getSuccessful(CacheEvent event,
                                                StationImpl station) {
        Query query = getSession().createQuery(successPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getFailed(StationImpl station) {
        Query query = getSession().createQuery(failed);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<AbstractEventChannelPair> getFailed(CacheEvent event,
                                            StationImpl station) {
        Query query = getSession().createQuery(failedPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getFailed(CacheEvent event) {
        Query query = getSession().createQuery(failedPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getRetry(StationImpl station) {
        Query query = getSession().createQuery(retry);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<AbstractEventChannelPair> getRetry(CacheEvent event) {
        Query query = getSession().createQuery(retryPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getRetry(CacheEvent event, StationImpl station) {
        Query query = getSession().createQuery(retryPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<StationImpl> getStationsForEvent(CacheEvent event) {
        String q = "select distinct ecp.esp.station from "
                + AbstractEventChannelPair.class.getName()
                + " ecp where ecp.event = :event";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<StationImpl> getSuccessfulStationsForEvent(CacheEvent event) {
        String q = "select distinct ecp.esp.station from "
                + AbstractEventChannelPair.class.getName()
                + " ecp where ecp.event = :event and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal();
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<StationImpl> getUnsuccessfulStationsForEvent(CacheEvent event) {
        String q = "from " + StationImpl.class.getName()
                + " s where s not in ("
                + "select distinct ecp.esp.station from "
                + AbstractEventChannelPair.class.getName()
                + " ecp where ecp.event = :event and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal()
                + " )";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<CacheEvent> getEventsForStation(StationImpl sta) {
        String q = "select distinct ecp.event from "
                + AbstractEventChannelPair.class.getName()
                + " ecp where ecp.esp.station = :sta ";
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public List<CacheEvent> getSuccessfulEventsForStation(StationImpl sta) {
        String q = "select distinct ecp.event from "
                + AbstractEventChannelPair.class.getName()
                + " ecp where ecp.esp.station = :sta  and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal();
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public List<CacheEvent> getUnsuccessfulEventsForStation(StationImpl sta) {
        String q = "from "
                + CacheEvent.class.getName()
                + " e where e not in ("
                + "select distinct ecp.event from "
                + AbstractEventChannelPair.class.getName()
                + " ecp where ecp.esp.station = :sta  and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal()
                + " )";
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public long put(RecordSectionItem item) {
        return ((Long)getSession().save(item)).longValue();
    }

    public RecordSectionItem getRecordSectionItemForEvent(CacheEvent event,
                                                  ChannelImpl channel) {
        String q = "from "
                + RecordSectionItem.class.getName()
                + " where event = :event and channel = :channel";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        query.setEntity("channel", channel);
        Iterator it = query.iterate();
        if(it.hasNext()) {
            return (RecordSectionItem)it.next();
        }
        return null;
    }

    public RecordSectionItem getRecordSectionItem(String orientationId,
                                                  String recordSectionId,
                                                  CacheEvent event,
                                                  ChannelImpl channel) {
        String q = "from "
                + RecordSectionItem.class.getName()
                + " where event = :event and channel = :channel and orientationId = :orientationId and recordSectionId = :recsecid";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        query.setEntity("channel", channel);
        query.setString("orientationId", orientationId);
        query.setString("recsecid", recordSectionId);
        Iterator it = query.iterate();
        if(it.hasNext()) {
            return (RecordSectionItem)it.next();
        }
        return null;
    }

    public List<StationImpl> getStationsForRecordSection(String orientationId,
                                                         CacheEvent event,
                                                         boolean best) {
        logger.debug("getStationsForRecordSection("+orientationId+", "+best);
        Query q = getSession().createQuery("select distinct channel.site.station from "
                + RecordSectionItem.class.getName()
                + " where orientationId = :orientationId and event = :event and inBest = :best");
        q.setEntity("event", event);
        q.setString("orientationId", orientationId);
        q.setBoolean("best", best);
        return q.list();
    }

    public List<StationImpl> getStationsForRecordSection(String orientationId,
                                                         String recordSectionId,
                                                         CacheEvent event,
                                                         boolean best) {
        logger.debug("getStationsForRecordSection("+orientationId+", "+recordSectionId+", "+best);
        Query q = getSession().createQuery("select distinct channel.site.station from "
                + RecordSectionItem.class.getName()
                + " where recordSectionId = :recsecid and orientationId = :orientationId and event = :event and inBest = :best");
        q.setEntity("event", event);
        q.setString("recsecid", recordSectionId);
        q.setString("orientationId", orientationId);
        q.setBoolean("best", best);
        return q.list();
    }

    public List<ChannelImpl> getChannelsForRecordSection(String orientationId,
                                                         CacheEvent event,
                                                         boolean best) {
        Query q = getSession().createQuery("select distinct channel from "
                + RecordSectionItem.class.getName()
                + " where orientationId = :orientationId and event = :event and inBest = :best");
        q.setEntity("event", event);
        q.setString("orientationId", orientationId);
        q.setBoolean("best", best);
        return q.list();
    }

    public List<RecordSectionItem> getBestForRecordSection(String orientationId,
                                                           String recordSectionId,
                                                           CacheEvent event) {
        Query q = getSession().createQuery("from "
                + RecordSectionItem.class.getName()
                + " where inBest = true and event = :event and orientationid = :orientationid and recordSectionId = :recsecid");
        q.setEntity("event", event);
        q.setString("orientationid", orientationId);
        q.setString("recsecid", recordSectionId);
        return q.list();
    }

    public boolean updateBestForRecordSection(String orientationId,
                                              String recordSectionId,
                                              CacheEvent event,
                                              ChannelId[] channelIds) {
        List<RecordSectionItem> best = getBestForRecordSection(orientationId,
                                                               recordSectionId,
                                                               event);
        HashMap<String, ChannelId> removes = new HashMap<String, ChannelId>();
        Iterator<RecordSectionItem> it = best.iterator();
        while(it.hasNext()) {
            ChannelId cId = it.next().channel.get_id();
            removes.put(ChannelIdUtil.toString(cId), cId);
        }
        HashMap<String, ChannelId> adders = new HashMap<String, ChannelId>();
        logger.debug("updating "+channelIds.length+" recordSectionItems for "+recordSectionId+" for event "+event);
        for(int i = 0; i < channelIds.length; i++) {
            logger.debug("channelid: "+ChannelIdUtil.toString(channelIds[i]));
            adders.put(ChannelIdUtil.toString(channelIds[i]), channelIds[i]);
        }
        Iterator<String> chanIt = adders.keySet().iterator();
        while(chanIt.hasNext()) {
            String cId = chanIt.next();
            if(removes.containsKey(cId)) {
                // in both, so no change
                removes.remove(cId);
                chanIt.remove();
            }
        }
        Query q;
        if(removes.size() == 0 && adders.size() == 0) {
            logger.debug("No adds and no removes");
            return false;
        }
        q = getSession().createQuery("from "
                + RecordSectionItem.class.getName()
                + " where inBest = true and event = :event and recordSectionId = :recsecid and orientationid = :orientationid and "
                + MATCH_CHANNEL_CODES);
        chanIt = removes.keySet().iterator();
        while(chanIt.hasNext()) {
            ChannelId c = removes.get(chanIt.next());
            logger.debug("remove: " + q + "  " + event.getDbid() + "  "
                    + recordSectionId + " " + c.channel_code + " "
                    + c.site_code + " " + c.station_code + " "
                    + c.network_id.network_code);
            q.setEntity("event", event);
            q.setString("orientationid", orientationId);
            q.setString("recsecid", recordSectionId);
            q.setString("chanCode", c.channel_code);
            q.setString("siteCode", c.site_code);
            q.setString("staCode", c.station_code);
            q.setString("netCode", c.network_id.network_code);
            Iterator dbit = q.iterate();
            while(dbit.hasNext()) {
                RecordSectionItem item = (RecordSectionItem)dbit.next();
                logger.debug("update false for "+ChannelIdUtil.toString(item.getChannel().get_id()));
                item.setInBest(false);
                getSession().update(item);
            }
        }
        q = getSession().createQuery("from "
                + RecordSectionItem.class.getName()
                + " where inBest = false and event = :event and recordSectionId = :recsecid and orientationid = :orientationid and "
                + MATCH_CHANNEL_CODES);
        chanIt = adders.keySet().iterator();
        while(chanIt.hasNext()) {
            ChannelId c = adders.get(chanIt.next());
            logger.debug("adds " + q + "  " + event.getDbid() + "  "
                    + recordSectionId + " " + c.channel_code + " "
                    + c.site_code + " " + c.station_code + " "
                    + c.network_id.network_code);
            q.setEntity("event", event);
            q.setString("orientationid", orientationId);
            q.setString("recsecid", recordSectionId);
            q.setString("chanCode", c.channel_code);
            q.setString("siteCode", c.site_code);
            q.setString("staCode", c.station_code);
            q.setString("netCode", c.network_id.network_code);
            Iterator dbit = q.iterate();
            while(dbit.hasNext()) {
                RecordSectionItem item = (RecordSectionItem)dbit.next();
                logger.debug("update true for "+ChannelIdUtil.toString(item.getChannel().get_id()));
                item.setInBest(true);
                getSession().update(item);
            }
        }
        return true;
    }

    private static final String MATCH_CHANNEL_CODES = " channel.id.channel_code = :chanCode and channel.id.site_code = :siteCode and "
            + "channel.id.station_code = :staCode and channel.site.station.networkAttr.id.network_code = :netCode";

    public List<RecordSectionItem> recordSectionsForEvent(CacheEvent event) {
        Query q = getSession().createQuery("from "
                + RecordSectionItem.class.getName() + " where event = :event");
        q.setEntity("event", event);
        return q.list();
    }

    public int putConfig(SodConfig sodConfig) {
        Integer dbid = (Integer)getSession().save(sodConfig);
        return dbid.intValue();
    }

    public SodConfig getCurrentConfig() {
        String q = "From edu.sc.seis.sod.SodConfig c ORDER BY c.time desc";
        Query query = getSession().createQuery(q);
        query.setMaxResults(1);
        List<SodConfig> result = query.list();
        if(result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public SodConfig getConfig(int configid) {
        return (SodConfig)getSession().get(edu.sc.seis.sod.SodConfig.class, configid);
    }

    public QueryTime getQueryTime(String serverName, String serverDNS) {
        String q = "From edu.sc.seis.sod.QueryTime q WHERE q.serverName = :serverName AND q.serverDNS = :serverDNS";
        Query query = getSession().createQuery(q);
        query.setString("serverName", serverName);
        query.setString("serverDNS", serverDNS);
        query.setMaxResults(1);
        List<QueryTime> result = query.list();
        if(result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public int putQueryTime(QueryTime qtime) {
        Integer dbid = (Integer)getSession().save(qtime);
        return dbid.intValue();
    }

    public Version getDBVersion() {
        String q = "From edu.sc.seis.sod.Version ORDER BY dbid desc";
        Session session = getSession();
        Query query = getSession().createQuery(q);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            Version out = (Version)result.get(0);
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

    private Queue<AbstractEventChannelPair> retryToDo = new LinkedList<AbstractEventChannelPair>();

    private Queue<EventNetworkPair> enpToDo = new LinkedList<EventNetworkPair>();
    
    private Queue<EventStationPair> espToDo = new LinkedList<EventStationPair>();

    private static String retry, failed, success, successPerEvent,
            failedPerEvent, retryPerEvent, successPerEventStation,
            failedPerEventStation, retryPerEventStation, totalSuccess,
            eventBase;

    private static final String COUNT = "SELECT COUNT(*) ";
    static {
        String baseStatement = "FROM "+AbstractEventChannelPair.class.getName()+" ecp WHERE ";
        String staBase = baseStatement + " ecp.esp.station = :sta ";
        String staEventBase = baseStatement
                + " ecp.esp.station = :sta and ecp.event = :event ";
        Status pass = Status.get(Stage.PROCESSOR, Standing.SUCCESS);
        String PROCESS_SUCCESS = " ecp.status.stageInt = "
                + pass.getStageInt()+" AND ecp.status.standingInt = "+pass.getStandingInt();
        eventBase = baseStatement + " ecp.event = :event ";
        success = staBase + " AND "+PROCESS_SUCCESS;
        String failReq = " AND ecp.status.standingInt in (" + Standing.REJECT.getVal() + " , "+Standing.SYSTEM_FAILURE.getVal()+")";
        failed = staBase +  failReq;
        String retryReq = " AND ecp.status.standingInt in (" + Standing.RETRY.getVal() + " , "+Standing.CORBA_FAILURE.getVal()+")";
        retry = staBase + retryReq ;
        successPerEvent = eventBase + " AND "+PROCESS_SUCCESS;
        failedPerEvent = eventBase + failReq;
        retryPerEvent = eventBase +  retryReq;
        successPerEventStation = staEventBase + "  AND "+PROCESS_SUCCESS;
        failedPerEventStation = staEventBase + failReq;
        retryPerEventStation = staEventBase + retryReq;
        totalSuccess = baseStatement + " "+PROCESS_SUCCESS;
    }

    public static SodDB getSingleton() {
        if(singleton == null) {
            singleton = new SodDB();
        }
        return singleton;
    }

    private static SodDB singleton;
}