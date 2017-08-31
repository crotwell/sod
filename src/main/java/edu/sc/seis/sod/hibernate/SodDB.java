package edu.sc.seis.sod.hibernate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.hibernate.LockMode;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunctionTemplate;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.QueryTime;
import edu.sc.seis.sod.RunProperties;
import edu.sc.seis.sod.SodConfig;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.VersionHistory;
import edu.sc.seis.sod.hibernate.eventpair.AbstractEventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.AbstractEventPair;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventNetworkPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.Version;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.util.time.ClockUtil;

public class SodDB extends AbstractHibernateDB {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SodDB.class);

    static String configFile = "edu/sc/seis/sod/hibernate/sod.hbm.xml";

    /** database should use one of EventVectorPair or EventChannelPair. Using 
     * AbstractEventChannelPair results in very slow union in selects in hsqldb. By
     * specifying which table we are using, the queries are several orders of magnitude
     * faster and do not use huge amounts of memory, which matters a lot when the 
     * number of ecps becomes large.
     */
    protected SodDB() {
    } // only for singleton
    

    public void reopenSuspendedEventChannelPairs(String processingRule, boolean vector) {
        Stage[] stages = {Stage.EVENT_CHANNEL_POPULATION,
                          Stage.EVENT_STATION_SUBSETTER,
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
        if(processingRule.equals(AT_LEAST_ONCE)) {
            setStmt = " stageInt = "+Stage.EVENT_CHANNEL_POPULATION.getVal()+", standingInt = "+Standing.INIT.getVal();
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

    public EventNetworkPair createEventNetworkPair(StatefulEvent event, Network net) {
        Session session = getSession();
        EventNetworkPair enp = new EventNetworkPair(event, 
                                                    (Network)session.merge(net),
                                                    Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                               Standing.INIT));
        logger.debug("Put "+enp);
        session.save(enp);
        synchronized(enpToDo) {
            enpToDo.offer(enp);
        }
        return enp;
    }

    public void offerEventNetworkPairs(List<EventNetworkPair> staPairList) {
        for (EventNetworkPair pair : staPairList) {
            synchronized(enpToDo) {
                enpToDo.offer(pair);
            }
        }
    }

    public void offerEventStationPair(List<EventStationPair> staPairList) {
        for (EventStationPair eventStationPair : staPairList) {
            synchronized(espToDo) {
                espToDo.offer(eventStationPair);
            }
        }
    }

    public void offerEventChannelPair(List<AbstractEventChannelPair> chanPairList) {
        for (AbstractEventChannelPair ecp : chanPairList) {
            synchronized(ecpToDo) {
                ecpToDo.offer(ecp);
            }
        }
    }
    
    public List<EventStationPair> loadESPForNetwork(StatefulEvent event, Network net) {
        if (espFromNet == null) { initHQLStmts(); }
        Query query = getSession().createQuery(espFromNet);
        query.setEntity("event", event);
        query.setEntity("net", net);
        return query.list();
    }
    
    public EventStationPair createEventStationPair(StatefulEvent event, Station station) {
        logger.debug("Put esp ("+event.getDbid()+",s "+station.getDbid()+") ");
        Session session = getSession();
        EventStationPair esp = new EventStationPair(event, 
                                                    (Station)session.merge(station),
                                                    Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                               Standing.INIT) );
        session.save(esp);
        synchronized(espToDo) {
            espToDo.offer(esp);
        }
        return esp;
    }

    public EventChannelPair createEventChannelPair(StatefulEvent event, Channel chan, EventStationPair esp) {
        Session session = getSession();
        EventChannelPair eventChannelPair = new EventChannelPair(event, 
                                                                 (Channel)session.merge(chan),
                                                                 esp);
        logger.debug("Put "+eventChannelPair);
        session.save(eventChannelPair);
        return eventChannelPair;
    }
    
    public boolean isECPTodo() {
        synchronized(ecpToDo) {
            return ! ecpToDo.isEmpty();
        }
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
            List<EventNetworkPair> beingLoaded = new ArrayList<EventNetworkPair>();
            enp = enpToDo.poll();
            while (enp != null && Start.getNetworkArm().isBeingRefreshed(enp.getNetwork())) {
                beingLoaded.add(enp);
                enp = enpToDo.poll();
            }
            enpToDo.addAll(beingLoaded); // put ones being reloaded back on queue
        }
        if (enp != null) {
            // might be new thread
            // ok to use even though might not be committed as hibernate flushes
            // due to native generator for id
            return (EventNetworkPair)getSession().merge(enp);
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
            return (EventStationPair)getSession().merge(esp);
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
                + " and e.status.standingInt = :standing ";
        Query query = getSession().createQuery(q);
        query.setInteger("standing", Standing.INIT.getVal());
        query.setMaxResults(1000);
        List<EventStationPair> result = query.list();
        for (EventStationPair eventStationPair : result) {
            synchronized(espToDo) {
                espToDo.offer(eventStationPair);
            }
        }
    }

    
    public synchronized void populateECPToDo() {
        logger.debug("populateECPToDo");
        String q = "from "
                + getEcpClass().getName()
                + " e "
                + " left join fetch e.event ";
        if (getEcpClass().equals(EventChannelPair.class)) {
        q +=  " left join fetch e.channel "
                + " left join fetch e.channel.site "
                + " left join fetch e.channel.site.station "
                + " left join fetch e.channel.site.station.networkAttr ";
        } else {
            q +=  " left join fetch e.channelGroup "
                    + " left join fetch e.channelGroup.channel1.site "
                    + " left join fetch e.channelGroup.channel1.site.station "
                    + " left join fetch e.channelGroup.channel1.site.station.networkAttr "
                    + " left join fetch e.channelGroup.channel2.site "
                    + " left join fetch e.channelGroup.channel2.site.station "
                    + " left join fetch e.channelGroup.channel2.site.station.networkAttr "
                    + " left join fetch e.channelGroup.channel3.site "
                    + " left join fetch e.channelGroup.channel3.site.station "
                    + " left join fetch e.channelGroup.channel3.site.station.networkAttr ";
        }
        q+=  " where e.status.stageInt = "+Stage.EVENT_CHANNEL_POPULATION.getVal()
                + " and e.status.standingInt = :standing ";
        Query query = getSession().createQuery(q);
        query.setInteger("standing", Standing.INIT.getVal());
        query.setMaxResults(1000);
        List<AbstractEventChannelPair> result = query.list();
        logger.info("populate ECP/EVP ToDo: "+result.size());
        for (AbstractEventChannelPair ecp : result) {
            synchronized(ecpToDo) {
                ecpToDo.offer(ecp);
            }
        }
        logger.debug("Done populateECPToDo "+result.size());
    }


    /** next successful event-station to process from memory cache. 
     * Returns null if no more esp in memory. */
    public synchronized AbstractEventChannelPair getNextECPFromCache() {
        AbstractEventChannelPair ecp;
        synchronized(ecpToDo) {
            ecp = ecpToDo.poll();
        }
        if (ecp != null) {
            // might be new thread
            // ok to use even though might not be committed as hibernate flushes
            // due to native generator for id
            return (AbstractEventChannelPair)getSession().merge(ecp);
        }
        return null;
    }
    
    /** next successful event-channel to process. Returns null if no more events. */
    public AbstractEventChannelPair getNextECP() {
        if ( ! isECPTodo()) {
            populateECPToDo();
        }
        return getNextECPFromCache();
    }

    public AbstractEventChannelPair getNextRetryECPFromCache() {
        AbstractEventChannelPair ecp;
        synchronized(retryToDo) {
            ecp = retryToDo.poll();
        }
        if (ecp != null) {
            return (AbstractEventChannelPair)getSession().merge(ecp);
        }
        return null;
    }
    
    public boolean isRetryTodo() {
        synchronized(retryToDo) {
            return ! retryToDo.isEmpty();
        }
    }

    public List<AbstractEventChannelPair> getRetryToDo() {
        logger.debug("Getting retry from db");
        String q = "from "
                + getEcpClass().getName()
                + "  where (status.standingInt = "
                + Standing.RETRY.getVal()
                + " or status.standingInt = "
                + Standing.CORBA_FAILURE.getVal()
                + " )  and seconds_between(:now, lastQuery) > :minDelay "
                + " and numRetries < "+maxRetries
                +" and (seconds_between(:now, lastQuery) > :maxDelay or seconds_between(:now, lastQuery) > power(:base, numRetries))  order by numRetries";
        Query query = getSession().createQuery(q);
        query.setTimestamp("now", ClockUtil.now());
        query.setFloat("base", retryBase);
        query.setFloat("minDelay", (float)getMinRetryDelay().getValue(UnitImpl.SECOND));
        query.setFloat("maxDelay", (float) (Start.getRunProps().getMaxRetryDelay().toNanos()/TimeRange.NANOS_IN_SEC));
        query.setMaxResults(10000);
        logger.info("retry query: "+q);
        List<AbstractEventChannelPair> result = query.list();
        logger.debug("retry query: "+q);
        return result;
    }

    public void populateRetryToDo() {
        List<AbstractEventChannelPair> result = getRetryToDo();
        for (AbstractEventChannelPair abstractEventChannelPair : result) {
            synchronized(retryToDo) {
                retryToDo.offer(abstractEventChannelPair);
            }
        }
        logger.debug("Got "+result.size()+" retries from db.");
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

    public EventChannelPair getECP(CacheEvent event, Channel chan) {
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
    Duration minRetryDelay = Duration.ofHours(2);
    
    public Duration getMinRetryDelay() {
        return minRetryDelay;
    }

    float maxRetryDelay = (float) (Start.getRunProps().getMaxRetryDelay().toNanos()/TimeRange.NANOS_IN_SEC);

    float seismogramLatency = (float) (Start.getRunProps()
            .getSeismogramLatency().toNanos()/TimeRange.NANOS_IN_SEC);

    int maxRetries = 5;

    float retryBase = 2;

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getNumSuccessful() {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + totalSuccess);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumSuccessful(CacheEvent event) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + successPerEvent);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumSuccessful(Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + success);
        query.setEntity("sta", station);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumSuccessful(CacheEvent event, Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + successPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumFailed(Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + failed);
        query.setEntity("sta", station);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumFailed(CacheEvent event, Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + failedPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumFailed(CacheEvent event) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + failedPerEvent);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumRetry(Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + retry);
        query.setEntity("sta", station);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumRetry(CacheEvent event) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + retryPerEvent);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public int getNumRetry(CacheEvent event, Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(COUNT + retryPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return ((Long)query.uniqueResult()).intValue();
    }

    public List<AbstractEventChannelPair> getAll(CacheEvent event) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(eventBase);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getSuccessful(CacheEvent event) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(successPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getSuccessful(Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(success);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<AbstractEventChannelPair> getSuccessful(CacheEvent event,
                                                Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(successPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getFailed(Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(failed);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<AbstractEventChannelPair> getFailed(CacheEvent event,
                                            Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(failedPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getFailed(CacheEvent event) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(failedPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getRetry(Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(retry);
        query.setEntity("sta", station);
        return query.list();
    }

    public List<AbstractEventChannelPair> getRetry(CacheEvent event) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(retryPerEvent);
        query.setEntity("event", event);
        return query.list();
    }

    public List<AbstractEventChannelPair> getRetry(CacheEvent event, Station station) {
        if (totalSuccess == null) { initHQLStmts(); }
        Query query = getSession().createQuery(retryPerEventStation);
        query.setEntity("sta", station);
        query.setEntity("event", event);
        return query.list();
    }

    public List<Station> getStationsForEvent(CacheEvent event) {
        String q = "select distinct ecp.esp.station from "
                + getEcpClass().getName()
                + " ecp where ecp.event = :event";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }
    
    public EventStationPair getEventStationPair(CacheEvent event, Station station) {
        String q = "from EventStationPair where event = :event and station = :station";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        query.setEntity("station", station);
        return (EventStationPair)query.uniqueResult();
    }

    public List<Station> getSuccessfulStationsForEvent(CacheEvent event) {
        String q = "select distinct ecp.esp.station from "
                + getEcpClass().getName()
                + " ecp where ecp.event = :event and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal();
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<Station> getUnsuccessfulStationsForEvent(CacheEvent event) {
        String q = "from " + Station.class.getName()
                + " s where s not in ("
                + "select distinct ecp.esp.station from "
                + getEcpClass().getName()
                + " ecp where ecp.event = :event and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal()
                + " )";
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }

    public List<EventStationPair> getSuccessfulESPForEvent(CacheEvent event) {
        String q = "from EventStationPair where event = :event and status.standingInt = "+ Standing.SUCCESS.getVal();
        Query query = getSession().createQuery(q);
        query.setEntity("event", event);
        return query.list();
    }
    
    public List<CacheEvent> getEventsForStation(Station sta) {
        String q = "select distinct ecp.event from "
                + getEcpClass().getName()
                + " ecp where ecp.esp.station = :sta ";
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }
//   and status.stageInt = " + Stage.PROCESSOR.getVal()+" and
    public List<EventStationPair> getSuccessfulESPForStation(Station sta) {
        String q = "from EventStationPair where station = :sta and status.standingInt = "+ Standing.SUCCESS.getVal();
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public List<StatefulEvent> getSuccessfulEventsForStation(Station sta) {
        String q = "select distinct ecp.event from "
                + getEcpClass().getName()
                + " ecp where ecp.esp.station = :sta  and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal();
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    public List<CacheEvent> getUnsuccessfulEventsForStation(Station sta) {
        String q = "from "
                + CacheEvent.class.getName()
                + " e where e not in ("
                + "select distinct ecp.event from "
                + getEcpClass().getName()
                + " ecp where ecp.esp.station = :sta  and ecp.status.stageInt = "
                + Stage.PROCESSOR.getVal()+" and ecp.status.standingInt = "+ Standing.SUCCESS.getVal()
                + " )";
        Query query = getSession().createQuery(q);
        query.setEntity("sta", sta);
        return query.list();
    }

    private static final String MATCH_CHANNEL_CODES = " channel.id.channel_code = :chanCode and channel.id.site_code = :siteCode and "
            + "channel.id.station_code = :staCode and channel.site.station.networkAttr.id.network_code = :netCode";


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

    public QueryTime getQueryTime(String serverName) {
        String q = "From edu.sc.seis.sod.QueryTime q WHERE q.serverName = :serverName";
        Query query = getSession().createQuery(q);
        query.setString("serverName", serverName);
        query.setMaxResults(1);
        List<QueryTime> result = query.list();
        if(result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public int putQueryTime(QueryTime qtime) {
        QueryTime indb = getQueryTime(qtime.getServerName());
        if (indb != null) {
            indb.setTime(qtime.getTime());
            getSession().saveOrUpdate(indb);
            return indb.getDbid();
        } else {
            Integer dbid = (Integer)getSession().save(qtime);
            return dbid.intValue();
        }
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
        Version v = VersionHistory.current();
        session.save(v);
        return v;
    }

    protected Version putDBVersion() {
        Version v = getDBVersion();
        Version current = VersionHistory.current();
        current.setDbid(v.getDbid());
        getSession().merge(current);
        commit();
        return current;
    }

    private Queue<AbstractEventChannelPair> retryToDo = new LinkedList<AbstractEventChannelPair>();

    private Queue<EventNetworkPair> enpToDo = new LinkedList<EventNetworkPair>();

    private Queue<EventStationPair> espToDo = new LinkedList<EventStationPair>();
    
    private Queue<AbstractEventChannelPair> ecpToDo = new LinkedList<AbstractEventChannelPair>();

    
    private String retry, failed, success, successPerEvent,
            failedPerEvent, retryPerEvent, successPerEventStation,
            failedPerEventStation, retryPerEventStation, totalSuccess,
            eventBase;

    private static final String COUNT = "SELECT COUNT(*) ";
    
    public void initHQLStmts() {
        String baseStatement = "FROM "+getEcpClass().getName()+" ecp WHERE ";
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
        espFromNet = "FROM "+EventStationPair.class.getName()+" esp WHERE "
            +" esp.event = :event and esp.station.networkAttr = :net";
    }
    
    private String espFromNet;
    
    public static Class<? extends AbstractEventChannelPair> discoverDbEcpClass() {
        Class<? extends AbstractEventChannelPair> out;
        try {
            String q = "from " + EventVectorPair.class.getName();
            Query query = getSession().createQuery(q);
            query.setMaxResults(1);
            List<EventChannelPair> result = query.list();
            if(result.size() > 0) {
                return EventVectorPair.class;
            } else {
                return EventChannelPair.class;
            }
        } catch(Throwable e) {
            logger.warn("Exception in SodDB.discoverDbEcpClass()", e);
            throw new RuntimeException("Exception in SodDB.discoverDbEcpClass()", e);
        } finally {
            rollback();
        }
    }
    
    public static void setDefaultEcpClass(Class<? extends AbstractEventChannelPair> ecpClass) {
        if (ecpClass == null) {throw new IllegalArgumentException("ECP Class cannot be null");}
        SodDB.defaultEcpClass = ecpClass;
        if (singleton != null && singleton.ecpClass != null && singleton.ecpClass != ecpClass) {
            throw new RuntimeException("Setting ecpClass but session is already open with different ecpClass: set("+ecpClass+") != "+singleton.ecpClass);
        }
    }
    
    public static Class<? extends AbstractEventChannelPair> defaultEcpClass = null;

    public Class<? extends AbstractEventChannelPair> ecpClass = null;

    public Class<? extends AbstractEventChannelPair> getEcpClass() {
        if (ecpClass == null) {
            if (defaultEcpClass == null) {
                defaultEcpClass = discoverDbEcpClass();
            }
            ecpClass = defaultEcpClass;
        }
        return ecpClass;
    }
    
    public static SodDB getSingleton() {
        synchronized(SodDB.class) {
            if(singleton == null) {
                singleton = new SodDB();
            }
        }
        return singleton;
    }

    private static SodDB singleton;
    

    public static final String AT_LEAST_ONCE = "atLeastOnce";

    public static final String AT_MOST_ONCE = "atMostOnce";
    
}
