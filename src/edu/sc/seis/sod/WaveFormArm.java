package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.database.*;
import edu.sc.seis.sod.subsetter.waveFormArm.*;

import edu.sc.seis.fissuresUtil.cache.*;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import java.util.*;
import java.io.*;
import java.sql.*;

import org.w3c.dom.*;
import org.apache.log4j.*;

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
        throws Exception 
    {
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
	
        this.config = config;
        this.networkArm = networkArm;
        addSodExceptionListener(sodExceptionListener);
        this.sodExceptionListener = sodExceptionListener;
        pool = new ThreadPool(threadPoolSize, this);
    }

    private synchronized void  restoreDb()  throws InvalidDatabaseStateException{
        //	Connection connection = Start.getWaveformQueue().getConnection(
        //	synchronized(connection) {
        int[] ids = Start.getWaveformQueue().getIds();

        for(int counter = 0; counter < ids.length; counter++) {
            int eventid = Start.getWaveformQueue().getWaveformEventId(ids[counter]);
            int channelid = Start.getWaveformQueue().getWaveformChannelId(ids[counter]);
            EventAccessOperations eventAccess = Start.getEventQueue().getEventAccess(eventid);
            updateChannelCount(eventid, channelid, eventAccess);
        }
        Start.getEventQueue().updateEventDatabase();
        //}
    }
	
    /**
     * run method of the waveformArm.
     *
     */
    public void run() {

        EventAccessOperations eventAccess = null;  
        int eventid;
	
		
        //ThreadPool pool = new ThreadPool(5);
        //getThreadGroup().list();
        try {
            restoreDb();

            int i = 0;
            //get the first event from the eventQueue.
            eventid = Start.getEventQueue().pop();
            logger.debug("The number of events in the queue is "+Start.getEventQueue().getLength());
            // if(Start.getEventQueue().getLength() < 4) notifyAll();
            //loop while thereis potential for new events.
            while(eventid != -1) {
		
		
                eventAccess = Start.getEventQueue().getEventAccess(eventid);
                waveformStatusProcess.begin(eventAccess);
                EventEffectiveTimeOverlap eventOverlap = 
                    new EventEffectiveTimeOverlap(eventAccess);
		
                Connection connection = Start.getWaveformQueue().getConnection();
                // if reopen begin the transaction.
                // Start.getWaveformQueue().beginTransaction();
		
                //get succesful Networks.
                NetworkDbObject[] networks = networkArm.getSuccessfulNetworks();
	
                Start.getWaveformQueue().putInfo(eventid, 0);//networks.length);
                if(networks.length == 0) {
                    updateEventStatus(eventid, eventAccess);
                }
                for(int netcounter = 0; netcounter < networks.length; netcounter++) {
                    waveformStatusProcess.begin(eventAccess, networks[netcounter].getNetworkAccess());

                    // don't bother with network if effective time does no
                    // overlap event time
                    if ( ! eventOverlap.overlaps(networks[netcounter].getNetworkAccess().get_attributes())) {
                        waveformStatusProcess.end(eventAccess, 
                                                    networks[netcounter].getNetworkAccess());
                        continue;
                    } // end of if ()
                    
                    //getSuccessful Stations
                    StationDbObject[] stations = networkArm.getSuccessfulStations(networks[netcounter]);
		    
                    //insert the neworkInfo into the waveformNetworkdb.
                    Start.getWaveformQueue().putNetworkInfo(eventid,
                                                            networks[netcounter].getDbId(),
                                                            0,///stations.length,
                                                            new MicroSecondDate());
                    if(stations.length == 0) {
                        updateNetworkCount(eventid, networks[netcounter].getDbId(), eventAccess);
                    }
                    for(int stationcounter = 0; stationcounter < stations.length; stationcounter++) {
			
                        waveformStatusProcess.begin(eventAccess, stations[stationcounter].getStation());

                        // don't bother with station if effective time does not
                        // overlap event time
                        if ( ! eventOverlap.overlaps(stations[stationcounter].getStation())) {
                            waveformStatusProcess.end(eventAccess, 
                                                      stations[stationcounter].getStation());
                            continue;
                        } // end of if ()
                    

                        //get Successful Sites.
                        SiteDbObject[] sites = networkArm.getSuccessfulSites(networks[netcounter], 
                                                                             stations[stationcounter]);
			
                        //insert the stationInfo into the waveformStationDb.
                        Start.getWaveformQueue().putStationInfo(eventid,
                                                                stations[stationcounter].getDbId(),
                                                                networks[netcounter].getDbId(),
                                                                0,//sites.length,
                                                                new MicroSecondDate());
                        if(sites.length == 0) {
                            updateStationCount(eventid, stations[stationcounter].getDbId(), eventAccess);
                        }
                        for(int sitecounter = 0; sitecounter < sites.length; sitecounter++) {
                            waveformStatusProcess.begin(eventAccess, sites[sitecounter].getSite());

                            // don't bother with site if effective time does not
                            // overlap event time
                            if ( ! eventOverlap.overlaps(sites[sitecounter].getSite())) {
                                waveformStatusProcess.end(eventAccess, 
                                                          sites[sitecounter].getSite());
                                continue;
                            } // end of if ()
                    
                            //get successful channels.
                            ChannelDbObject[] successfulChannels = 
                                networkArm.getSuccessfulChannels(networks[netcounter], sites[sitecounter]);

                            Start.getWaveformQueue().putSiteInfo(eventid,
                                                                 sites[sitecounter].getDbId(),
                                                                 stations[stationcounter].getDbId(),
                                                                 0,//successfulChannels.length,
                                                                 new MicroSecondDate());
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
                                    channelDbObjectMap.put( (new Integer(successfulChannels[counter].getDbId())).toString(),
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
                Start.getEventQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(), 
                                                     Status.AWAITING_FINAL_STATUS);
                //	Start.getWaveformQueue().endTransaction();
                //get the next event.
                eventid = 
                    Start.getEventQueue().pop();
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
		} catch(Exception e) {
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
                if(sodElement instanceof EventStationSubsetter) eventStationSubsetter = (EventStationSubsetter)sodElement;
                else if(sodElement instanceof LocalSeismogramArm) localSeismogramArm = (LocalSeismogramArm)sodElement;
              
                else if(sodElement instanceof WaveformStatusProcess) waveformStatusProcess = (WaveformStatusProcess)sodElement;

            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)

    }

    public synchronized void signalWaveFormArm()  {
        notifyAll();
    }

    //  public synchronized int getNetworkId(Channel channel) {
    // 	return networkArm.getNetworkId(channel);
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

    public synchronized void setFinalStatus(EventDbObject eventDbObject,
                                            ChannelDbObject channelDbObject,
                                            Status status,
                                            String reason) throws InvalidDatabaseStateException{
        EventAccessOperations eventAccess = eventDbObject.getEventAccess();
        Channel channel = channelDbObject.getChannel();
        int eventid = eventDbObject.getDbId();
        int channelid = channelDbObject.getDbId();
        //if(status.getId() == Status.PROCESSING.getId()) return;
        Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventid, channelid),
                                           status, reason);
        waveformStatusProcess.end(eventAccess, channel, status, reason);
        if(status.getId() == Status.PROCESSING.getId()) return;
        updateChannelCount(eventid, channelid, eventAccess);

    }

    private void write(String str) {
        try {
            FileWriter fw = new FileWriter("stationdebugger", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str, 0, str.length());
            bw.newLine();
            bw.close();
            fw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    
    private synchronized void updateChannelCount(int eventid, 
                                                 int channelid, 
                                                 EventAccessOperations eventAccess) throws InvalidDatabaseStateException{
        Connection connection = Start.getWaveformQueue().getConnection();
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
            write("Channel count afer is "+Start.getWaveformQueue().getChannelCount(eventid, sitedbid)+
                  "channelid is "+channelid+" eventid is "+eventid);
            //	Start.getWaveformQueue().deleteChannelInfo(eventid, channelid);

            boolean flag = false;
            if(count <= 1 ) {
                //decrement corresponding station reference count..
                //delete the corresponding entry from the wavefrom sitedb
                flag = true;
                updateSiteCount(eventid, sitedbid, eventAccess);
            }
        }
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
        write("site count before is "+count+" the siteid is "+sitedbid
              +" eventid is "+eventid);
        if(count > 0 && count > unfinishedCount) {
            Start.getWaveformQueue().decrementSiteCount(eventid, stationdbid);

            count = Start.getWaveformQueue().getSiteCount(eventid, stationdbid);
            if(count == -1) {
                throw new InvalidDatabaseStateException("site Count is -1");
            }
        }
        write("site count after is "+Start.getWaveformQueue().getSiteCount(eventid, stationdbid)+
              "the siteid is "+sitedbid+" eventid is "+eventid);
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
        write("the station count before is "+count+" the stationid is "+stationdbid+
              " eventid is "+eventid);

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
        write("the station count after is "+Start.getWaveformQueue().getStationCount(eventid, networkdbid)+
              " the stationid is "+stationdbid+" evetnid is "+eventid);
        //	    Start.getWaveformQueue().deleteStationInfo(eventid, stationdbid);
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
        write("the network count before is "+count+" the networkid is "+networkdbid+
              " eventid is "+eventid);

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
        write("the network count after is "+Start.getWaveformQueue().getNetworkCount(eventid)+
              " the networkid is "+networkdbid+" evetnid is "+eventid);	
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
        Start.getEventQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(), 
                                             Status.COMPLETE_SUCCESS);
        if(Start.getEventQueue().getLength() == 0) waveformStatusProcess.closeProcessing();
	
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
	
        // 	if(change) {
        // 	    Status status = Start.getEventQueue().getStatus(waveformeventid);
        // 	    EventAccessOperations eventAccess = Start.getEventQueue().getEventAccess(waveformeventid);
        // 	    Start.getEventQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(), 
        // 						 Status.AWAITING_FINAL_STATUS);
        // 	}
    }


    /***
     * This class represents the ThreadWorker which will be continuously gets
     * new work from the ThreadPool, finishes the work and again looks for new work
     *  until it is signaled to stop looking for new work.
     */

    class ThreadWorker extends Thread {

        ThreadWorker(ThreadPool pool) {
            this.pool = pool;
        }

        boolean finished = false;
        ThreadPool pool;

        /**
         * used to signal the end of new work.
         */
	
        public void finish() {
            finished = true;
            logger.debug("SETTING THE FINISHED TO TRUE FOR "+getName());
        }

        public void run() {
            Runnable work = pool.getWork();

            while ( ! finished && work != null) {
                work.run();
                ///getThreadGroup().list();
                work = pool.getWork();
		
            } // end of while ( ! finished)
            //getThreadGroup().list();
            logger.debug("EXITING THE RUN METHOD OF WORKER");

        }
    }

    
    /**
     * This class maintains a pool of workerThreads. When the WorkerThreads ask
     * for new work it gets the next Object from the waveformQueue, constructs a
     * Runnable and gives it to the worker thread. This class also signals the worker
     * when there is no more new work. 
     *
     */

    class ThreadPool {

        ThreadPool(int n, WaveFormArm waveformArm) {
            this.waveformArm = waveformArm;
            for (int i=0; i<n; i++) {
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

	    	    
            int waveformid = Start.getWaveformQueue().pop();
            if(waveformid == -1) return null;
            int eventid = Start.getWaveformQueue().getWaveformEventId(waveformid);
            int channelid = Start.getWaveformQueue().getWaveformChannelId(waveformid);
	 
            Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventid, channelid),
                                               Status.PROCESSING, "just after being popped");

            EventAccessOperations eventAccess = null;
            EventDbObject eventDbObject = null;
            ChannelDbObject channelDbObject = null;
            try {
	
                //EventAccessOperations 
                eventAccess =
                    Start.getEventQueue().getEventAccess(eventid);
                Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventid, channelid),
                                                   Status.PROCESSING, "got eventAccess");

                //EventDbObject 
                eventDbObject = new EventDbObject(eventid, eventAccess);
                //ChannelDbObject channelDbObject = null;
                if(	((ChannelDbObject)channelDbObjectMap.get((new Integer(channelid)).toString())) != null) {
                    logger.debug("******** Channnel already in the hasmamp");
                    channelDbObject = ((ChannelDbObject)channelDbObjectMap.get((new Integer(channelid)).toString()));
                    Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventid, channelid),
                                                       Status.PROCESSING, "got channeldbobject from map");
			
                } else {
                    channelDbObject = new ChannelDbObject(channelid, networkArm.getChannel(channelid));
                }
            } catch(Throwable ce) {
                ce.printStackTrace();
            }
            //  setFinalStatus(eventDbObject, channelDbObject,
            // 	    Status.PROCESSING, "still didnot even form the waveformThread Runnable");
            if(eventid == 11 && channelid == 27) {
                logger.debug("got the needed one");
                logger.debug("The eventid is "+eventDbObject.getDbId());
                logger.debug("The channelid is "+channelDbObject.getDbId());

            }
            //networkArm.getChannel(networkid);
            NetworkAccess networkAccess = null;
            NetworkDbObject networkDbObject = null;
            ChannelDbObject[] paramChannels = new ChannelDbObject[1];
            try {
                int siteid =
                    networkArm.getSiteDbId(channelid);
                int stationid = networkArm.getStationDbId(siteid);
                int networkid = networkArm.getNetworkDbId(stationid);
                //NetworkAccess 
                networkAccess = networkArm.getNetworkAccess(networkid);
                //NetworkDbObject 
                networkDbObject = new NetworkDbObject(networkid, networkAccess);
                //ChannelDbObject[] paramChannels = new ChannelDbObject[1];
                paramChannels[0] = channelDbObject;
            } catch(Throwable ce) {
                ce.printStackTrace();
                /*setFinalStatus(eventDbObject, channelDbObject, 
                  Status.PROCESSING,
                  "Exception occured ");*/

            }
            Runnable work = new WaveFormArmThread(eventDbObject, 
                                                  eventStationSubsetter,
                                                  localSeismogramArm,
                                                  networkDbObject,
                                                  paramChannels,
                                                  waveformArm,
                                                  sodExceptionListener);
            return work;
	    
            //   logger.debug("INSIDETHE METHOD GETWORK");
            // 	    while (work == null && ! finished) {
            // 		try {
            // 		    logger.debug("Waiting in the getWork METHOD");
            // 		    logger.debug("Before Wait in getWork of ThreadPool");
            // 		    wait();
            // 		    logger.debug("After Wait in getWork of ThreadPool");
            // 		} catch (InterruptedException e) { }
            // 	    }
            // 	    if (finished) {
            // 		logger.debug("finished everything so just...returning null");
            // 		notifyAll();
            // 		return null;
            // 	    } // end of if (finished)
            // 	    logger.debug("returning mywork");
            // 	    Runnable myWork = work;
            // 	    work = null;
            // 	    notifyAll();
            // 	    return myWork;
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

        private WaveFormArm waveformArm;
    }

    
    

    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();

    private ThreadPool pool;

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm = null;
    
    private Element config = null;

 
    private SodExceptionListener sodExceptionListener;

    private WaveformStatusProcess waveformStatusProcess = new NullWaveformStatusProcess();

    HashMap channelDbObjectMap = new HashMap();

    HashMap eventDbObjectMap = new HashMap();

    static Category logger = 
        Category.getInstance(WaveFormArm.class.getName());
    
}
 
