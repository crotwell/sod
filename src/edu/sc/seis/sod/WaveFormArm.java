package edu.sc.seis.sod;

import edu.sc.seis.sod.database.*;

import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.subsetter.waveFormArm.EventEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.waveFormArm.LocalSeismogramArm;
import edu.sc.seis.sod.subsetter.waveFormArm.NullEventStationSubsetter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class WaveFormArm extends SodExceptionSource implements Runnable {
    
    /**
     * Creates a new <code>WaveFormArm</code> instance.
     *
     * @param config an <code>Element</code> value
     * @param networkArm a <code>NetworkArm</code> value
     */
    public WaveFormArm(Element config,
                       NetworkArm networkArm,
                       SodExceptionListener sodExceptionListener)
        throws Exception {
        this(config, networkArm, sodExceptionListener, 5);
    }
    
    /**
     * Creates a new <code>WaveFormArm</code> instance.
     *
     * @param config an <code>Element</code> value
     * @param networkArm a <code>NetworkArm</code> value
     */
    public WaveFormArm(Element config,
                       NetworkArm networkArm,
                       SodExceptionListener sodExceptionListener,
                       int threadPoolSize)
        throws Exception {
        if ( ! config.getTagName().equals("waveFormArm")) {
            throw new IllegalArgumentException("Configuration element must be a waveFormArm tag");
        }
        processConfig(config);
        this.networkArm = networkArm;
        addSodExceptionListener(sodExceptionListener);
        this.sodExceptionListener = sodExceptionListener;
        pool = new ThreadPool(threadPoolSize);
    }
    
    private void  restoreDb()  throws InvalidDatabaseStateException{
        Object connection = Start.getWaveformQueue().getConnection();
        synchronized(connection) {
            long[] ids = Start.getWaveformQueue().getIds();
            for(int counter = 0; counter < ids.length; counter++) {
                int eventid = Start.getWaveformQueue().getWaveformEventId(ids[counter]);
                int channelid = Start.getWaveformQueue().getWaveformChannelId(ids[counter]);
                EventAccessOperations eventAccess = Start.getEventQueue().getEventAccess(eventid);
                updateChannelCount(eventid, channelid, eventAccess);
            }
            Start.getEventQueue().updateEventDatabase();
        }
    }
    
    public void run() {
        try {
            restoreDb();
            //get the first event from the eventQueue.
            int eventid = Start.getEventQueue().pop();
            //loop while thereis potential for new events.
            while(eventid != -1) {
                logger.debug("The number of events in the queue is "+Start.getEventQueue().getLength());
                EventAccessOperations eventAccess = Start.getEventQueue().getEventAccess(eventid);
                EventEffectiveTimeOverlap eventOverlap =
                    new EventEffectiveTimeOverlap(eventAccess);
                Object connection = Start.getWaveformQueue().getConnection();
                logger.debug("got connection to the waveFormQueue");
                
                NetworkDbObject[] networks = networkArm.getSuccessfulNetworks();
                logger.debug("got " + networks.length + " networks from getSuccessfulNetworks()");
                
                Start.getWaveformQueue().putInfo(eventid, 0);//networks.length);
                logger.debug("put the event into the queue");
                
                if(networks.length == 0) updateEventStatus(eventid, eventAccess);
                for(int netcounter = 0; netcounter < networks.length; netcounter++) {
                    // don't bother with network if effective time does no
                    // overlap event time
                    if ( ! eventOverlap.overlaps(networks[netcounter].getNetworkAccess().get_attributes())) {
                        logger.debug("The networks effective time does not overlap the event time");
                        continue;
                    } // end of if ()
                    
                    StationDbObject[] stations = networkArm.getSuccessfulStations(networks[netcounter]);
                    logger.debug("got " + stations.length + " SuccessfulStations");
                    
                    Start.getWaveformQueue().putNetworkInfo(eventid,
                                                            networks[netcounter].getDbId(),
                                                            0,///stations.length,
                                                            ClockUtil.now());
                    if(stations.length == 0) {
                        updateNetworkCount(eventid, networks[netcounter].getDbId(), eventAccess);
                    }
                    for(int stationcounter = 0; stationcounter < stations.length; stationcounter++) {
                        if ( ! eventOverlap.overlaps(stations[stationcounter].getStation())) {
                            logger.debug("The stations effective time does not overlap the event time");
                            continue;
                        } // end of if ()
                        SiteDbObject[] sites = networkArm.getSuccessfulSites(networks[netcounter],
                                                                             stations[stationcounter]);
                        logger.debug("got " + sites.length + " SuccessfulSites");
                        
                        Start.getWaveformQueue().putStationInfo(eventid,
                                                                stations[stationcounter].getDbId(),
                                                                networks[netcounter].getDbId(),
                                                                0,//sites.length,
                                                                ClockUtil.now());
                        if(sites.length == 0) {
                            updateStationCount(eventid, stations[stationcounter].getDbId(), eventAccess);
                        }
                        for(int sitecounter = 0; sitecounter < sites.length; sitecounter++) {
                            if ( ! eventOverlap.overlaps(sites[sitecounter].getSite())) {
                                logger.debug("The sites effective time does not overlap the event time");
                                continue;
                            } // end of if ()
                            
                            ChannelDbObject[] successfulChannels =
                                networkArm.getSuccessfulChannels(networks[netcounter], sites[sitecounter]);
                            logger.debug("got " + successfulChannels.length + " SuccessfulChannels");
                            Start.getWaveformQueue().putSiteInfo(eventid,
                                                                 sites[sitecounter].getDbId(),
                                                                 stations[stationcounter].getDbId(),
                                                                 0,//successfulChannels.length,
                                                                 ClockUtil.now());
                            if(successfulChannels.length == 0) {
                                updateSiteCount(eventid, sites[sitecounter].getDbId(), eventAccess);
                            }
                            
                            //when the threads are really changed to per channel base ..
                            // the below for loop must be removed...
                            for(int counter = 0; counter < successfulChannels.length; counter++) {
                                //here add the channel to the channel database.
                                //increment the channel count.
                                //increment the siteCount if necessary.
                                //increment the stationCount if necessary.
                                //increment the networkCount if necessary.
                                synchronized(connection) {
                                    
                                    //cache the channelInformation.
                                    channelDbCache.put( new Integer(successfulChannels[counter].getDbId()),
                                                       successfulChannels[counter]);
                                    
                                    //push the channel to the waveformQueue.
                                    Start.getWaveformQueue().push(eventid,
                                                                  sites[sitecounter].getDbId(),
                                                                  successfulChannels[counter].getDbId());
                                    
                                    //update Rerefence counts of channels, stations,
                                    //sites and networks.
                                    updateReferences(eventid, successfulChannels[counter].getDbId());
                                } //end of synchronization block
                                // pool.doWork(work)
                            }
                        }//end of for sitecounter
                    }//end of for stationcounter
                }//end of for netcounter
                
                //set the status of the event to be AWAITING_FINAL_STATUS implying that
                //that all the network information for this particular event is inserted
                //in the waveformDatabase.
                Start.getEventQueue().setFinalStatus(eventAccess,
                                                     Status.AWAITING_FINAL_STATUS);
                //  Start.getWaveformQueue().endTransaction();
                //get the next event.
                eventid = Start.getEventQueue().pop();
            }
            
            //signals the waveformQueue the end of
            //processing of all the events.
            Start.getWaveformQueue().setSourceAlive(false);
            pool.join();
            
            //make the state of the database consistent.
            //this statement is required to make
            //the state of the waveform database consistent.
            restoreDb();
            
        } catch(InvalidDatabaseStateException idse) {
            logger.fatal("Invalid database StateException", idse);
            notifyListeners(this, idse);
        } catch(Throwable e) {
            logger.fatal("Problem running waveform arm", e);
            notifyListeners(this, e);
        } finally {
            //pool.finished();
        } // end of finally
        
    }
    
    /**
     * processes the configuration file checking for waveformArmSubsetter types.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    protected void processConfig(Element config)
        throws ConfigurationException {
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node,"edu.sc.seis.sod.subsetter.waveFormArm");
                if(sodElement instanceof EventStationSubsetter){
                    eventStationSubsetter = (EventStationSubsetter)sodElement;
                }else if(sodElement instanceof LocalSeismogramArm){
                    localSeismogramArm = (LocalSeismogramArm)sodElement;
                }else if(sodElement instanceof WaveFormStatus){
                    statusMonitors.add(sodElement);
                }else {
                    System.err.println("Unkown tag "+((Element)node).getTagName()+" found in config file");
                    System.exit(1);
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
        
    }
    
    public synchronized void signalWaveFormArm()  {
        notifyAll();
    }
    
    //  public synchronized int getNetworkId(Channel channel) {
    //  return networkArm.getNetworkId(channel);
    //     }
    
    
    /**
     * returns databaseid corresponding to the eventAccess.
     */
    public synchronized int getEventId(EventAccess eventAccess) {
        return Start.getEventQueue().getEventId(eventAccess);
    }
    
    
    /**
     * sets the finalStatus of the event. For infomation about the status see
     * the class Status in the package edu.sc.seis.sod.database.
     */
    
    public synchronized void setStatus(EventChannelPair ecp) throws InvalidDatabaseStateException{
        EventAccessOperations eventAccess = ecp.getEvent();
        int eventid = ecp.getEventDbId();
        int channelid = ecp.getChannelDbId();
        Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventid, channelid),
                                           ecp.getStatus(), ecp.getInfo());
        Iterator it = statusMonitors.iterator();
        while(it.hasNext()){
            ((WaveFormStatus)it.next()).update(ecp);
        }
        if(ecp.getStatus().getId() == Status.PROCESSING.getId()) return;
        updateChannelCount(eventid, channelid, eventAccess);
    }
    
    private void updateChannelCount(int eventid,
                                    int channelid,
                                    EventAccessOperations eventAccess) throws InvalidDatabaseStateException{
        Object connection = Start.getWaveformQueue().getConnection();
        synchronized(connection) {
            int sitedbid = networkArm.getSiteDbId(channelid);
            int count = Start.getWaveformQueue().getChannelCount(eventid, sitedbid);
            int unfinishedCount = Start.getWaveformQueue().unfinishedChannelCount(eventid, sitedbid);
            logger.debug("Channel count before is "+count+" the channelid is "+channelid+
                             "eventid is  "+eventid);
            if(count == -1) { throw new InvalidDatabaseStateException("Channel Count is -1"); }
            if(count > 0 && count > unfinishedCount) {
                Start.getWaveformQueue().decrementChannelCount(eventid, sitedbid);
                count = Start.getWaveformQueue().getChannelCount(eventid, sitedbid);
                if(count == -1) {
                    throw new InvalidDatabaseStateException("Channel Count is -1");
                }
            }
            if(count <= 1 ) {
                //decrement corresponding station reference count..
                //delete the corresponding entry from the wavefrom sitedb
                updateSiteCount(eventid, sitedbid, eventAccess);
            }
        }//end of synch
    }
    
    private synchronized void updateSiteCount(int eventid,
                                              int sitedbid,
                                              EventAccessOperations eventAccess) throws InvalidDatabaseStateException{
        
        boolean flag = false;
        int stationdbid = networkArm.getStationDbId(sitedbid);
        int count = Start.getWaveformQueue().getSiteCount(eventid, stationdbid);
        int unfinishedCount = Start.getWaveformQueue().unfinishedSiteCount(eventid, stationdbid);
        
        if(count == -1) {
            throw new InvalidDatabaseStateException("site Count is -1");
        }
        
        if(count > 0 && count > unfinishedCount) {
            Start.getWaveformQueue().decrementSiteCount(eventid, stationdbid);
            
            count = Start.getWaveformQueue().getSiteCount(eventid, stationdbid);
            if(count == -1) {
                throw new InvalidDatabaseStateException("site Count is -1");
            }
        }
        
        //  Start.getWaveformQueue().deleteSiteInfo(eventid, sitedbid);
        if(count <= 1) {
            flag = true;
            updateStationCount(eventid, stationdbid, eventAccess);
        }
        
    }
    
    private synchronized void updateStationCount(int eventid,
                                                 int stationdbid,
                                                 EventAccessOperations eventAccess) throws InvalidDatabaseStateException{
        
        boolean flag = false;
        int networkdbid = networkArm.getNetworkDbId(stationdbid);
        int count = Start.getWaveformQueue().getStationCount(eventid, networkdbid);
        int unfinishedCount = Start.getWaveformQueue().unfinishedStationCount(eventid, networkdbid);
        
        
        if(count == -1) {
            throw new InvalidDatabaseStateException("station Count is -1");
        }
        
        if(count > 0 && count > unfinishedCount) {
            Start.getWaveformQueue().decrementStationCount(eventid, networkdbid);
            
            count = Start.getWaveformQueue().getStationCount(eventid, networkdbid);
            if(count == -1) {
                throw new InvalidDatabaseStateException("station Count is -1");
            }
        }
        
        //      Start.getWaveformQueue().deleteStationInfo(eventid, stationdbid);
        if(count <= 1) {
            flag = true;
            updateNetworkCount(eventid, networkdbid, eventAccess);
        }
    }
    
    private synchronized void updateNetworkCount(int eventid,
                                                 int networkdbid,
                                                 EventAccessOperations eventAccess) throws InvalidDatabaseStateException{
        
        boolean flag = false;
        int count = Start.getWaveformQueue().getNetworkCount(eventid);
        int unfinishedCount = Start.getWaveformQueue().unfinishedNetworkCount(eventid);
        
        
        if(count == -1) {
            throw new InvalidDatabaseStateException("Network Count is -1");
        }
        
        if(count > 0 && count > unfinishedCount) {
            Start.getWaveformQueue().decrementNetworkCount(eventid);
            
            count = Start.getWaveformQueue().getNetworkCount(eventid);
            if(count == -1) {
                throw new InvalidDatabaseStateException("Network Count is -1");
            }
            
        }
        
        //    Start.getWaveformQueue().deleteNetworkInfo(eventid, networkdbid);
        if(count <= 1) {
            flag = true;
            updateEventStatus(eventid, eventAccess);
        }
    }
    
    private synchronized void updateEventStatus(int eventid,
                                                EventAccessOperations eventAccess) throws InvalidDatabaseStateException{
        //delete the row corresponding to eventid from
        // the waveform database.
        //   Start.getWaveformQueue().deleteInfo(eventid);
        Status status = Start.getEventQueue().getStatus(eventid);
        if(status.getId() != Status.AWAITING_FINAL_STATUS.getId()) return;
        Start.getEventQueue().setFinalStatus((CacheEvent)eventAccess,
                                             Status.COMPLETE_SUCCESS);
        //        if(Start.getEventQueue().getLength() == 0) //waveformStatusProcess.closeProcessing();
        
    }
    
    private void updateReferences(int waveformeventid, int channeldbid) {
        int sitedbid = networkArm.getSiteDbId(channeldbid);
        int count = Start.getWaveformQueue().getChannelCount(waveformeventid,
                                                             sitedbid);
        int  networkdbid = -1, stationdbid = -1;
        Start.getWaveformQueue().incrementChannelCount(waveformeventid,
                                                       sitedbid);
        boolean  change = false;
        if(count == 0) {
            change = true;
        }
        if(change) {
            change = false;
            stationdbid = networkArm.getStationDbId(sitedbid);
            count = Start.getWaveformQueue().getSiteCount(waveformeventid,
                                                          stationdbid);
            Start.getWaveformQueue().incrementSiteCount(waveformeventid,
                                                        stationdbid);
            if(count == 0) change = true;
        }
        
        if(change) {
            change = false;
            networkdbid = networkArm.getNetworkDbId(stationdbid);
            count = Start.getWaveformQueue().getStationCount(waveformeventid,
                                                             networkdbid);
            Start.getWaveformQueue().incrementStationCount(waveformeventid,
                                                           networkdbid);
            if(count == 0) change = true;
        }
        
        if(change) {
            change = false;
            count = Start.getWaveformQueue().getNetworkCount(waveformeventid);
            Start.getWaveformQueue().incrementNetworkCount(waveformeventid);
            if(count == 0) change = true;
        }
        
        //  if(change) {
        //      Status status = Start.getEventQueue().getStatus(waveformeventid);
        //      EventAccessOperations eventAccess = Start.getEventQueue().getEventAccess(waveformeventid);
        //      Start.getEventQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(),
        //                       Status.AWAITING_FINAL_STATUS);
        //  }
    }
    
    
    /***
     * This class represents the ThreadWorker which will be continuously gets
     * new work from the ThreadPool, finishes the work and again looks for new work
     *  until it is signaled to stop looking for new work.
     */
    private class ThreadWorker extends Thread {
        
        public ThreadWorker(ThreadPool pool) {
            this.pool = pool;
        }
        
        public void finish() {
            finished = true;
            logger.debug("SETTING THE FINISHED TO TRUE FOR "+getName());
        }
        
        public void run() {
            Runnable work = pool.getWork();
            while ( ! finished && work != null) {
                work.run();
                work = pool.getWork();
                
            } // end of while ( ! finished)
            logger.debug("EXITING THE RUN METHOD OF WORKER");
        }
        
        private boolean finished = false;
        
        private ThreadPool pool;
    }
    
    
    /**
     * This class maintains a pool of workerThreads. When the WorkerThreads ask
     * for new work it gets the next Object from the waveformQueue, constructs a
     * Runnable and gives it to the worker thread. This class also signals the worker
     * when there is no more new work.
     *
     */
    private class ThreadPool {
        ThreadPool(int n) {
            for (int i=0; i< (n); i++) {
                Thread t = new ThreadWorker(this);
                t.setName("waveFormArm Worker Thread"+i);
                pool.add(t);
                t.start();
            } // end of for (int i=0; i<n; i++)
            
        }
        
        private LinkedList pool = new LinkedList();
        
        private Runnable work = null;
        
        public synchronized void doWork(Runnable workUnit) {
            while (work != null) {
                try {
                    logger.debug("waiting in doWork method The queue is size "+Start.getEventQueue().getLength());
                    
                    logger.debug("Before Wait in do Work of ThreadPool");
                    wait();
                    logger.debug("After Wait in do work of ThreadPool");
                } catch (InterruptedException e) { }
            }
            work = workUnit;
            notifyAll();
        }
        
        public synchronized Runnable getWork() {
            long id = Start.getWaveformQueue().pop();
            if(id == -1) return null;
            int eventId = Start.getWaveformQueue().getWaveformEventId(id);
            int channelId = Start.getWaveformQueue().getWaveformChannelId(id);
            Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventId, channelId),
                                               Status.PROCESSING, "just after being popped");
            EventAccessOperations eventAccess = Start.getEventQueue().getEventAccess(eventId);
            Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventId, channelId),
                                               Status.PROCESSING, "got eventAccess");
            EventDbObject eventDbObject = new EventDbObject(eventId, eventAccess);
            ChannelDbObject channelDbObject = null;
            Integer key = new Integer(channelId);
            if( channelDbCache.containsKey(key)) {
                logger.debug("******** Channnel already in the hasmamp");
                channelDbObject = (ChannelDbObject)channelDbCache.get(key);
                
            } else {
                channelDbObject = new ChannelDbObject(channelId, networkArm.getChannel(channelId));
                channelDbCache.put(key, channelDbObject);
            }
            Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventId, channelId),
                                               Status.PROCESSING, "got channeldbobject");
            int siteid = networkArm.getSiteDbId(channelId);
            int stationid = networkArm.getStationDbId(siteid);
            int networkid = networkArm.getNetworkDbId(stationid);
            NetworkAccess networkAccess  = networkArm.getNetworkAccess(networkid);
            NetworkDbObject networkDbObject = new NetworkDbObject(networkid, networkAccess);
            ChannelDbObject[] paramChannels = {channelDbObject};
            return new WaveFormArmProcessor(eventDbObject, eventStationSubsetter,
                                            localSeismogramArm, networkDbObject,
                                            paramChannels, WaveFormArm.this,
                                            sodExceptionListener);
        }
        
        public void join() {
            Iterator it = pool.iterator();
            while(it.hasNext()) {
                try {
                    ThreadWorker worker = ((ThreadWorker)it.next());
                    logger.debug("****** JOINING THE thread "+worker.getName());
                    worker.join();
                } catch(Exception e) {}
            }
            logger.debug("RETURNING FROM THE JOIN METHOD OF THE POOL ");
        }
        
        public synchronized void finished() {
            // wait until there is no more work waiting for a thread
            while (work != null) {
                try {
                    wait();
                } catch (InterruptedException e) { }
            }
            finished = true;
            Iterator it = pool.iterator();
            while (it.hasNext()) {
                ((ThreadWorker)it.next()).finish();
            } // end of while (it.hasNext())
            logger.debug("Finished the calling finish method of each Thread Worker");
            it = pool.iterator();
            while (it.hasNext()) {
                notifyAll();
                it.next();
            }
            logger.debug("Returning from the finished method");
            
        }
        
        private boolean finished = false;
    }
    
    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();
    
    private ThreadPool pool;
    
    private LocalSeismogramArm localSeismogramArm = null;
    
    private NetworkArm networkArm = null;
    
    private SodExceptionListener sodExceptionListener;
    
    private HashMap channelDbCache = new HashMap();
    
    private static Logger logger = Logger.getLogger(WaveFormArm.class);
    
    private List statusMonitors = new ArrayList();
}
