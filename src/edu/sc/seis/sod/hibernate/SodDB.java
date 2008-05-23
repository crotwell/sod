package edu.sc.seis.sod.hibernate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.hibernate.AbstractHibernateDB;
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

    public static void configHibernate(Configuration config) {
        logger.debug("adding to HibernateUtil   " + configFile);
        config.addResource(configFile);
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

    public SodDB() {}

    public EventChannelPair[] getSuspendedEventChannelPairs(String processingRule,
                                                            boolean vector)
            throws SQLException {
        Status eventStationInit = Status.get(Stage.EVENT_STATION_SUBSETTER,
                                             Standing.INIT);
        Status processorSuccess = Status.get(Stage.PROCESSOR, Standing.SUCCESS);
        Stage[] stages = {Stage.EVENT_STATION_SUBSETTER,
                          Stage.EVENT_CHANNEL_SUBSETTER,
                          Stage.REQUEST_SUBSETTER,
                          Stage.AVAILABLE_DATA_SUBSETTER,
                          Stage.DATA_RETRIEVAL,
                          Stage.PROCESSOR};
        Standing[] standings = {Standing.IN_PROG,
                                Standing.INIT,
                                Standing.SUCCESS};
        String query;
        if(!vector) {
            query = "FROM edu.sc.seis.sod.EventChannelPair e WHERE e.statusAsShort in";
        } else {
            query = "select evp.ecp1 FROM " + EventVectorPair.class.getName()
                    + " evp WHERE evp.ecp1.statusAsShort in";
        }
        query += "  ( ";
        for(int i = 0; i < stages.length; i++) {
            for(int j = 0; j < standings.length; j++) {
                Status curStatus = Status.get(stages[i], standings[j]);
                if(!curStatus.equals(processorSuccess)) {
                    query += curStatus.getAsShort();
                    query += ", ";
                }
            }
        }
        // get rid of last comma
        query = query.substring(0, query.length() - 2);
        query += ')';
        List out = getSession().createQuery(query).list();
        if(processingRule.equals(RunProperties.AT_LEAST_ONCE)) {
            return (EventChannelPair[])out.toArray(new EventChannelPair[0]);
        } else {
            ArrayList<EventChannelPair> reprocess = new ArrayList<EventChannelPair>();
            Iterator it = out.iterator();
            while(it.hasNext()) {
                EventChannelPair pair = (EventChannelPair)it.next();
                Status curStatus = pair.getStatus();
                Stage currentStage = curStatus.getStage();
                if(!curStatus.equals(eventStationInit)) {
                    pair.update(Status.get(currentStage,
                                           Standing.SYSTEM_FAILURE));
                } else {
                    reprocess.add(pair);
                }
            }
            flush();
            return reprocess.toArray(new EventChannelPair[0]);
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
        Class updateClass;
        if(!vector) {
            updateClass = EventChannelPair.class;
        } else {
            updateClass = EventVectorPair.class;
        }
        String setStmt;
        if(processingRule.equals(RunProperties.AT_LEAST_ONCE)) {
            setStmt = " stageInt = "+Stage.EVENT_CHANNEL_SUBSETTER.getVal()+", standing = "+Standing.INIT.getVal();
        } else {
            setStmt = " standing = "+Standing.SYSTEM_FAILURE.getVal();
        }
        query = "UPDATE "+updateClass.getName()+" set "+setStmt
        +" WHERE stageInt in "+stageList+" AND stangingInt in "+standingList
        +" AND NOT (stageInt = "+Stage.PROCESSOR.getVal()+" AND standingInt = "+Standing.SUCCESS.getVal()+" ) "
        +" AND NOT (stageInt = "+Stage.EVENT_STATION_SUBSETTER.getVal()+" AND standingInt = "+Standing.INIT.getVal()+" ) ";
        
        List out = getSession().createQuery(query).list();
    }

    public EventNetworkPair put(EventNetworkPair eventNetworkPair) {
        Session session = getSession();
        session.lock(eventNetworkPair.getNetwork(), LockMode.NONE);
        session.lock(eventNetworkPair.getEvent(), LockMode.NONE);
        session.saveOrUpdate(eventNetworkPair);
        return eventNetworkPair;
    }

    public EventStationPair put(EventStationPair eventStationPair) {
        Session session = getSession();
        session.lock(eventStationPair.getStation(), LockMode.NONE);
        session.lock(eventStationPair.getEvent(), LockMode.NONE);
        session.saveOrUpdate(eventStationPair);
        return eventStationPair;
    }

    public EventChannelPair put(EventChannelPair eventChannelPair) {
        Session session = getSession();
        session.lock(eventChannelPair.getChannel(), LockMode.NONE);
        session.lock(eventChannelPair.getEvent(), LockMode.NONE);
        session.saveOrUpdate(eventChannelPair);
        return eventChannelPair;
    }

    /** next successful event-network to process. Returns null if no more events. */
    public EventNetworkPair getNextENP(Standing standing) {
        String q = "from "
                + EventNetworkPair.class.getName()
                + " e where e.stageInt = Stage.EVENT_CHANNEL_POPULATION and e.standingInt = :inProg";
        Query query = getSession().createQuery(q);
        query.setInteger("inProg", standing.getVal());
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            return (EventNetworkPair)result.get(0);
        }
        return null;
    }

    /** next successful event-station to process. Returns null if no more events. */
    public EventStationPair getNextESP(Standing standing) {
        String q = "from "
                + EventStationPair.class.getName()
                + " e where e.stageInt = Stage.EVENT_CHANNEL_POPULATION and e.standingInt = :inProg";
        Query query = getSession().createQuery(q);
        query.setInteger("inProg", standing.getVal());
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            return (EventStationPair)result.get(0);
        }
        return null;
    }

    /** next successful event-channel to process. Returns null if no more events. */
    public EventChannelPair getNextECP(Standing standing) {
        String q = "from "
                + EventChannelPair.class.getName()
                + " e where e.stageInt = Stage.EVENT_CHANNEL_POPULATION and e.standingInt = :inProg";
        Query query = getSession().createQuery(q);
        query.setInteger("inProg", standing.getVal());
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            return (EventChannelPair)result.get(0);
        }
        return null;
    }

    public EventChannelPair getNextRetryECP() {
        String q = "from "
                + EventChannelPair.class.getName()
                + "  where numRetries > 0 and (standingInt = "
                + Standing.RETRY.getVal()
                + " or standingInt = "
                + Standing.CORBA_FAILURE.getVal()
                + " )  and seconds_between(:now, lastQuery) > :minDelay "
                + " and numRetries < "+maxRetries
                +" and (seconds_between(:now, lastQuery) > :maxDelay or seconds_between(:now, lastQuery) > power(:base, numRetries))  order by lastQuery desc";
        Query query = getSession().createQuery(q);
        query.setTimestamp("now", ClockUtil.now().getTimestamp());
        query.setFloat("base", retryBase);
        query.setFloat("minDelay", minRetryDelay);
        query.setFloat("maxDelay", maxRetryDelay);
        query.setMaxResults(1);
        EventChannelPair out = (EventChannelPair)query.uniqueResult();
        return out;
    }

    /** next successful event-vector to process. Returns null if no more events. */
    public EventVectorPair getNextEVP(Standing standing) {
        String q = "from "
                + EventVectorPair.class.getName()
                + " e where e.stageInt = Stage.EVENT_CHANNEL_POPULATION and e.standingInt = :inProg";
        Query query = getSession().createQuery(q);
        query.setInteger("inProg", standing.getVal());
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            return (EventVectorPair)result.get(0);
        }
        return null;
    }

    public EventVectorPair getNextRetryEVP() {
        String q = "from "
                + EventVectorPair.class.getName()
                + "  where numRetries > 0 and (standingInt = "
                + Standing.RETRY.getVal()
                + " or standingInt = "
                + Standing.CORBA_FAILURE.getVal()
                + " )  and seconds_between(:now, lastQuery) > :minDelay "
                + " and numRetries < "+maxRetries
                +" and (seconds_between(:now, lastQuery) > :maxDelay or seconds_between(:now, lastQuery) > power(:base, numRetries))  order by lastQuery desc";
        Query query = getSession().createQuery(q);
        query.setTimestamp("now", ClockUtil.now().getTimestamp());
        query.setFloat("base", retryBase);
        query.setFloat("minDelay", minRetryDelay);
        query.setFloat("maxDelay", maxRetryDelay);
        query.setMaxResults(1);
        EventVectorPair out = (EventVectorPair)query.uniqueResult();
        return out;
    }

    public int getNumWorkUnits(Standing standing) {
        String q = "select count(*) from " + AbstractEventPair.class.getName()
                + " e where e.statusAsShort = :inProg and e.numRetries ==  0";
        Query query = getSession().createQuery(q);
        query.setShort("inProg", Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                            standing).getAsShort());
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            return (Integer)result.get(0);
        }
        return 0;
    }

    public EventChannelPair getECP(CacheEvent event, ChannelImpl chan) {
        Query query = getSession().createQuery("from "
                + EventChannelPair.class.getName()
                + " where event = :event and channel = :channel");
        query.setEntity("event", event);
        query.setEntity("channel", chan);
        return ((EventChannelPair)query.uniqueResult());
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

    public EventVectorPair getEventVectorPair(EventChannelPair ecp) {
        String q = "from " + EventVectorPair.class.getName()
                + " where ecp1 = :ecp or ecp2 = :ecp or ecp3 = :ecp";
        Session session = getSession();
        Query query = session.createQuery(q);
        query.setEntity("ecp", ecp);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            EventVectorPair out = (EventVectorPair)result.get(0);
            return out;
        }
        return null;
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

    public List<EventChannelPair> getAll(CacheEvent event) {
        Query query = getSession().createQuery(eventBase);
        query.setEntity("event", event);
        return query.list();
    }

    public List<EventChannelPair> getSuccessful(CacheEvent event) {
        Query query = getSession().createQuery(successPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<EventChannelPair> getSuccessful(StationImpl station) {
        Query query = getSession().createQuery(success);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<EventChannelPair> getSuccessful(CacheEvent event,
                                                StationImpl station) {
        Query query = getSession().createQuery(successPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<EventChannelPair> getFailed(StationImpl station) {
        Query query = getSession().createQuery(failed);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<EventChannelPair> getFailed(CacheEvent event,
                                            StationImpl station) {
        Query query = getSession().createQuery(failedPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<EventChannelPair> getFailed(CacheEvent event) {
        Query query = getSession().createQuery(failedPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<EventChannelPair> getRetry(StationImpl station) {
        Query query = getSession().createQuery(retry);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<EventChannelPair> getRetry(CacheEvent event) {
        Query query = getSession().createQuery(retryPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<EventChannelPair> getRetry(CacheEvent event, StationImpl station) {
        Query query = getSession().createQuery(retryPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<StationImpl> getStationsForEvent(CacheEvent event) {
        String q = "select distinct ecp.channel.site.station from "
                + EventChannelPair.class.getName()
                + " ecp where ecp.event = :event";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<StationImpl> getSuccessfulStationsForEvent(CacheEvent event) {
        String q = "select distinct ecp.channel.site.station from "
                + EventChannelPair.class.getName()
                + " ecp where ecp.event = :event and ecp.statusAsShort = "
                + Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<StationImpl> getUnsuccessfulStationsForEvent(CacheEvent event) {
        String q = "from " + StationImpl.class.getName()
                + " s where s not in ("
                + "select distinct ecp.channel.site.station from "
                + EventChannelPair.class.getName()
                + " ecp where ecp.event = :event and ecp.statusAsShort = "
                + Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort()
                + " )";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<CacheEvent> getEventsForStation(StationImpl sta) {
        String q = "select distinct ecp.event from "
                + EventChannelPair.class.getName()
                + " ecp where ecp.channel.site.station = :sta ";
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public List<CacheEvent> getSuccessfulEventsForStation(StationImpl sta) {
        String q = "select distinct ecp.event from "
                + EventChannelPair.class.getName()
                + " ecp where ecp.channel.site.station = :sta  and ecp.statusAsShort = "
                + Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public List<CacheEvent> getUnsuccessfulEventsForStation(StationImpl sta) {
        String q = "from "
                + CacheEvent.class.getName()
                + " e where e not in ("
                + "select distinct ecp.event from "
                + EventChannelPair.class.getName()
                + " ecp where ecp.channel.site.station = :sta  and ecp.statusAsShort = "
                + Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort()
                + " )";
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public int put(RecordSectionItem item) {
        return ((Integer)getSession().save(item)).intValue();
    }

    public RecordSectionItem getRecordSectionItem(String recordSectionId,
                                                  CacheEvent event,
                                                  ChannelImpl channel) {
        String q = "from "
                + RecordSectionItem.class.getName()
                + " where event = :event and channel = :channel and recordSectionId = :recsecid";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        query.setEntity("channel", channel);
        query.setString("recsecid", recordSectionId);
        query.uniqueResult();
        Iterator it = query.iterate();
        if(it.hasNext()) {
            return (RecordSectionItem)it.next();
        }
        return null;
    }

    public List<StationImpl> getStationsForRecordSection(String recordSectionId,
                                                         CacheEvent event,
                                                         boolean best) {
        Query q = getSession().createQuery("select distinct channel.site.station from "
                + RecordSectionItem.class.getName()
                + " where recordSectionId = :recsecid and event = :event and inBest = :best");
        q.setEntity("event", event);
        q.setString("recsecid", recordSectionId);
        q.setBoolean("best", best);
        return q.list();
    }

    public List<ChannelImpl> getChannelsForRecordSection(String recordSectionId,
                                                         CacheEvent event,
                                                         boolean best) {
        Query q = getSession().createQuery("select distinct channel from "
                + RecordSectionItem.class.getName()
                + " where recordSectionId = :recsecid and event = :event and inBest = :best");
        q.setEntity("event", event);
        q.setString("recsecid", recordSectionId);
        q.setBoolean("best", best);
        return q.list();
    }

    public List<RecordSectionItem> getBestForRecordSection(String recordSectionId,
                                                           CacheEvent event) {
        Query q = getSession().createQuery("from "
                + RecordSectionItem.class.getName()
                + " where inBest = true and event = :event and recordSectionId = :recsecid");
        q.setEntity("event", event);
        q.setString("recsecid", recordSectionId);
        return q.list();
    }

    public boolean updateBestForRecordSection(String recordSectionId,
                                              CacheEvent event,
                                              ChannelId[] channelIds) {
        List<RecordSectionItem> best = getBestForRecordSection(recordSectionId,
                                                               event);
        Set<ChannelId> removes = new HashSet<ChannelId>();
        Iterator<RecordSectionItem> it = best.iterator();
        while(it.hasNext()) {
            removes.add(it.next().channel.get_id());
        }
        Set<ChannelId> adders = new HashSet<ChannelId>();
        for(int i = 0; i < channelIds.length; i++) {
            adders.add(channelIds[i]);
        }
        Iterator<ChannelId> chanIt = adders.iterator();
        while(chanIt.hasNext()) {
            Object o = chanIt.next();
            if(removes.contains(o)) {
                // in both, so no change
                removes.remove(o);
                adders.remove(o);
            }
        }
        Query q;
        if(removes.size() == 0 && adders.size() == 0) {
            return false;
        }
        q = getSession().createQuery("from "
                + RecordSectionItem.class.getName()
                + " where inBest = true and event = :event and recordSectionId = :recsecid and "
                + MATCH_CHANNEL_CODES);
        chanIt = removes.iterator();
        while(chanIt.hasNext()) {
            ChannelId c = chanIt.next();
            logger.debug("remove: " + q + "  " + event.getDbid() + "  "
                    + recordSectionId + " " + c.channel_code + " "
                    + c.site_code + " " + c.station_code + " "
                    + c.network_id.network_code);
            q.setEntity("event", event);
            q.setString("recsecid", recordSectionId);
            q.setString("chanCode", c.channel_code);
            q.setString("siteCode", c.site_code);
            q.setString("staCode", c.station_code);
            q.setString("netCode", c.network_id.network_code);
            Iterator dbit = q.iterate();
            while(dbit.hasNext()) {
                RecordSectionItem item = (RecordSectionItem)dbit.next();
                item.setInBest(false);
            }
        }
        q = getSession().createQuery("from "
                + RecordSectionItem.class.getName()
                + " where inBest = false and event = :event and recordSectionId = :recsecid and "
                + MATCH_CHANNEL_CODES);
        chanIt = adders.iterator();
        while(chanIt.hasNext()) {
            ChannelId c = chanIt.next();
            logger.debug("adds " + q + "  " + event.getDbid() + "  "
                    + recordSectionId + " " + c.channel_code + " "
                    + c.site_code + " " + c.station_code + " "
                    + c.network_id.network_code);
            q.setEntity("event", event);
            q.setString("recsecid", recordSectionId);
            q.setString("chanCode", c.channel_code);
            q.setString("siteCode", c.site_code);
            q.setString("staCode", c.station_code);
            q.setString("netCode", c.network_id.network_code);
            Iterator dbit = q.iterate();
            while(dbit.hasNext()) {
                RecordSectionItem item = (RecordSectionItem)dbit.next();
                item.setInBest(true);
            }
        }
        return true;
    }

    private static final String MATCH_CHANNEL_CODES = " channel.id.channel_code = :chanCode and channel.id.site_code = :siteCode and "
            + "channel.id.station_code = :staCode and channel.site.station.networkAttr.id.network_code = :netCode";

    public List<String> recordSectionsForEvent(CacheEvent event) {
        Query q = getSession().createQuery("select distinct recordSectionId from "
                + RecordSectionItem.class.getName() + " where event = :event");
        q.setEntity("event", event);
        return q.list();
    }

    public void putCookie(EcpCookie cookie) {
        getSession().saveOrUpdate(cookie);
    }

    public EcpCookie getCookie(EventChannelPair ecp, String name) {
        Query q = getSession().createQuery("from " + EcpCookie.class.getName()
                + " where ecp = :ecp and name = :name");
        q.setEntity("ecp", ecp);
        q.setString("name", name);
        return (EcpCookie)q.uniqueResult();
    }

    public List<String> getCookieNames(EventChannelPair ecp) {
        Query q = getSession().createQuery("select name from "
                + EcpCookie.class.getName() + " where ecp = :ecp");
        q.setEntity("ecp", ecp);
        return q.list();
    }

    public void deleteCookie(EcpCookie cookie) {
        getSession().delete(cookie);
    }

    public int putConfig(SodConfig sodConfig) {
        Integer dbid = (Integer)getSession().save(sodConfig);
        return dbid.intValue();
    }

    public SodConfig getCurrentConfig() {
        String q = "From edu.sc.seis.sod.SodConfig c ORDER BY c.time desc";
        Query query = getSession().createQuery(q);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            SodConfig out = (SodConfig)result.get(0);
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
        if(result.size() > 0) {
            QueryTime out = (QueryTime)result.get(0);
            return out;
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

    private static String retry, failed, success, successPerEvent,
            failedPerEvent, retryPerEvent, successPerEventStation,
            failedPerEventStation, retryPerEventStation, totalSuccess,
            eventBase;

    private static final String COUNT = "SELECT COUNT(*) ";
    static {
        String baseStatement = "FROM edu.sc.seis.sod.EventChannelPair ecp WHERE ";
        String staBase = baseStatement + " ecp.channel.site.station = :sta ";
        String staEventBase = baseStatement
                + " ecp.channel.site.station = :sta and ecp.event = :event ";
        eventBase = baseStatement + " ecp.event = :event ";
        int pass = Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
        success = staBase + " AND ecp.statusAsShort = " + pass;
        String failReq = getFailedStatusRequest();
        failed = staBase + " AND ecp.statusAsShort in " + failReq;
        String retryReq = getRetryStatusRequest();
        retry = staBase + " AND ecp.statusAsShort in " + retryReq;
        successPerEvent = eventBase + " AND ecp.statusAsShort = "
                + Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
        failedPerEvent = eventBase + "  AND ecp.statusAsShort in " + failReq;
        retryPerEvent = eventBase + "  AND ecp.statusAsShort in " + retryReq;
        successPerEventStation = staEventBase + "  AND ecp.statusAsShort = "
                + pass;
        failedPerEventStation = staEventBase + " AND ecp.statusAsShort in "
                + failReq;
        retryPerEventStation = staEventBase + "  AND ecp.statusAsShort in "
                + retryReq;
        totalSuccess = baseStatement + " ecp.statusAsShort = " + pass;
    }

    public static SodDB getSingleton() {
        if(singleton == null) {
            singleton = new SodDB();
        }
        return singleton;
    }

    private static SodDB singleton;
}