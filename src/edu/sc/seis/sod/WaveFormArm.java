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

import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * Describe class <code>WaveFormArm</code> here.
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
	logger.debug("IN the constructor of WAVEFORMARM THREAD");
	if ( ! config.getTagName().equals("waveFormArm")) {
	    throw new IllegalArgumentException("Configuration element must be a waveFormArm tag");
	}
	//logger.debug("In waveForm Arm");
	processConfig(config);
	
	this.config = config;
	this.networkArm = networkArm;
	addSodExceptionListener(sodExceptionListener);
	this.sodExceptionListener = sodExceptionListener;
	pool = new ThreadPool(threadPoolSize, this);
    }

    private void restoreDb() {
	int[] ids = Start.getWaveformQueue().getIds();
	for(int counter = 0; counter < ids.length; counter++) {
	    int eventid = Start.getWaveformQueue().getWaveformEventId(ids[counter]);
	    int channelid = Start.getWaveformQueue().getWaveformEventId(ids[counter]);
	    EventAccessOperations eventAccess = Start.getEventQueue().getEventAccess(eventid);
	    updateChannelCount(eventid, channelid, eventAccess);
	}
    }
	
    /**
     * Describe <code>run</code> method here.
     *
     */
    public void run() {
	EventAccessOperations eventAccess = null;  
	int eventid;
	restoreDb();
	Start.getEventQueue().updateEventDatabase();
		
	//ThreadPool pool = new ThreadPool(5);
	//getThreadGroup().list();
		try {
	    System.out.println("IN The waveform Arm Thread before POPPING");
	    int i = 0;
	    eventid = Start.getEventQueue().pop();
	    logger.debug("The queue is size "+Start.getEventQueue().getLength());
	    // if(Start.getEventQueue().getLength() < 4) notifyAll();
	    while(eventid != -1) {
		eventAccess =
		    Start.getEventQueue().getEventAccess(eventid);
				waveformStatusProcess.begin(eventAccess);
		NetworkDbObject[] networks = networkArm.getSuccessfulNetworks();
	
		Start.getWaveformQueue().putInfo(eventid, networks.length);
		if(networks.length == 0) {
		    updateEventStatus(eventid, eventAccess);
		}
		for(int netcounter = 0; netcounter < networks.length; netcounter++) {
		    waveformStatusProcess.begin(eventAccess, networks[netcounter].getNetworkAccess());
		    StationDbObject[] stations = networkArm.getSuccessfulStations(networks[netcounter]);
		    
		    Start.getWaveformQueue().putNetworkInfo(eventid,
							    networks[netcounter].getDbId(),
							    stations.length,
							    new MicroSecondDate());
		    if(stations.length == 0) {
			updateNetworkCount(eventid, networks[netcounter].getDbId(), eventAccess);
		    }
		    for(int stationcounter = 0; stationcounter < stations.length; stationcounter++) {
			waveformStatusProcess.begin(eventAccess, stations[stationcounter].getStation());
			SiteDbObject[] sites = networkArm.getSuccessfulSites(networks[netcounter], 
									     stations[stationcounter]);
			
			Start.getWaveformQueue().putStationInfo(eventid,
								stations[stationcounter].getDbId(),
								sites.length,
								new MicroSecondDate());
			if(sites.length == 0) {
			    updateStationCount(eventid, stations[stationcounter].getDbId(), eventAccess);
			}
			for(int sitecounter = 0; sitecounter < sites.length; sitecounter++) {
			    waveformStatusProcess.begin(eventAccess, sites[sitecounter].getSite());
			    ChannelDbObject[] successfulChannels = 
				networkArm.getSuccessfulChannels(networks[netcounter], sites[sitecounter]);
			    
			    Start.getWaveformQueue().putSiteInfo(eventid,
								 sites[sitecounter].getDbId(),
								 successfulChannels.length,
								 new MicroSecondDate());
			    if(successfulChannels.length == 0) {
				updateSiteCount(eventid, sites[sitecounter].getDbId(), eventAccess);
			    }
			    //when the threads are really changed to per channel base ..
			    // the below for loop must be removed...
			    for(int counter = 0; counter < successfulChannels.length; counter++) {
				channelDbObjectMap.put( (new Integer(successfulChannels[counter].getDbId())).toString(),
						     successfulChannels[counter]);
				
				Start.getWaveformQueue().push(eventid, successfulChannels[counter].getDbId());
				// pool.doWork(work);
			    }
		
			
			}//end of for sitecounter
		    }//end of for stationcounter
		}//end of for netcounter
		eventid = 
		    Start.getEventQueue().pop();
	    }
	    logger.debug("CALLING THE FINISHED METHOD OF THE POOL");
	    Start.getWaveformQueue().setSourceAlive(false);
	    pool.join(); 
	  	}  catch(Exception e) {
		    logger.fatal("Problem running waveform arm", e);
		    notifyListeners(this, e);
		} finally {
	    //pool.finished();
	} // end of finally
	
    }

    /**
     * Describe <code>processConfig</code> method here.
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
                else if(sodElement instanceof SeismogramDCLocator) seismogramDCLocator = (SeismogramDCLocator)sodElement; 
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

    public synchronized int getEventId(EventAccess eventAccess) {
	return Start.getEventQueue().getEventId(eventAccess);
    }

    public synchronized void setFinalStatus(EventDbObject eventDbObject,
					    ChannelDbObject channelDbObject,
					    Status status,
					    String reason) {
	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	Channel channel = channelDbObject.getChannel();
	int eventid = eventDbObject.getDbId();
	int channelid = channelDbObject.getDbId();

	Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventid, channelid),
					   status, reason);
	waveformStatusProcess.end(eventAccess, channel, status, reason);
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
    
    private synchronized void updateChannelCount(int eventid, int channelid, EventAccessOperations eventAccess) {
	int sitedbid = networkArm.getSiteDbId(channelid);
	      
	Start.getWaveformQueue().decrementChannelCount(eventid, sitedbid);
	//	Start.getWaveformQueue().deleteChannelInfo(eventid, channelid);

	boolean flag = false;
	if(Start.getWaveformQueue().getChannelCount(eventid, sitedbid) <= 0) {
	    //decrement corresponding station reference count..
	    //delete the corresponding entry from the wavefrom sitedb
	    flag = true;
	    updateSiteCount(eventid, sitedbid, eventAccess);
	}
    }
	
    private synchronized void updateSiteCount(int eventid, int sitedbid, EventAccessOperations eventAccess) {
	
	boolean flag = false;
	int stationdbid = networkArm.getStationDbId(sitedbid);

	Start.getWaveformQueue().decrementSiteCount(eventid, stationdbid);

	//  Start.getWaveformQueue().deleteSiteInfo(eventid, sitedbid);
	if(Start.getWaveformQueue().getSiteCount(eventid, stationdbid) <= 0) {
	    flag = true;
	    updateStationCount(eventid, stationdbid, eventAccess);
	}

    }
    
    private synchronized void updateStationCount(int eventid, int stationdbid, EventAccessOperations eventAccess) {

	boolean flag = false;
	int networkdbid = networkArm.getNetworkDbId(stationdbid);

	Start.getWaveformQueue().decrementStationCount(eventid, networkdbid);

	//	    Start.getWaveformQueue().deleteStationInfo(eventid, stationdbid);
	if(Start.getWaveformQueue().getStationCount(eventid, networkdbid) <= 0) {
	    flag = true;
	    updateNetworkCount(eventid, networkdbid, eventAccess);
	}
    }

    private synchronized void updateNetworkCount(int eventid, int networkdbid, EventAccessOperations eventAccess) {
	
	boolean flag = false;

	Start.getWaveformQueue().decrementNetworkCount(eventid);

	//    Start.getWaveformQueue().deleteNetworkInfo(eventid, networkdbid);
	if(Start.getWaveformQueue().getNetworkCount(eventid) <= 0) {
	    flag = true;
	    updateEventStatus(eventid, eventAccess);
	}
    }
    
    private synchronized void updateEventStatus(int eventid, EventAccessOperations eventAccess) {
	    //delete the row corresponding to eventid from
	    // the waveform database.
	    //   Start.getWaveformQueue().deleteInfo(eventid);
	Start.getEventQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(), 
						 Status.COMPLETE_SUCCESS);
	if(Start.getEventQueue().getLength() == 0) waveformStatusProcess.closeProcessing();
	
    }

    class ThreadWorker extends Thread {

	ThreadWorker(ThreadPool pool) {
	    this.pool = pool;
	}

	boolean finished = false;
	ThreadPool pool;

	public void finish() {
	    finished = true;
	System.out.println("SETTING THE FINISHED TO TRUE FOR "+getName());
	}

	public void run() {
	    Runnable work = pool.getWork();
	    logger.debug("In the run method of the worker before the while loop");
	    while ( ! finished && work != null) {
		logger.debug("Starting the Worker Thread");
		work.run();
		logger.debug("THe active count of thread is ");
		///getThreadGroup().list();
		logger.debug("GO AND GET NEW WORK");
		work = pool.getWork();
		
		logger.debug("AFTER GETTING WOEK");
	    } // end of while ( ! finished)
	    //getThreadGroup().list();
	    logger.debug("EXITING THE RUN METHOD OF WORKER");

	}
    }

    

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
		      //System.exit(0);
		      System.out.println("Before Wait in do Work of ThreadPool");
		      wait();
		      System.out.println("After Wait in do work of ThreadPool");
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
	    
	    EventAccessOperations eventAccess =
		Start.getEventQueue().getEventAccess(eventid);
	    EventDbObject eventDbObject = new EventDbObject(eventid, eventAccess);
	    ChannelDbObject channelDbObject = null;
	    if(	((ChannelDbObject)channelDbObjectMap.get((new Integer(channelid)).toString())) != null) {
		channelDbObject = ((ChannelDbObject)channelDbObjectMap.get((new Integer(channelid)).toString()));
	    } else {
		channelDbObject = new ChannelDbObject(channelid, networkArm.getChannel(channelid));
	    }
	    //networkArm.getChannel(networkid);
	    int siteid =
		networkArm.getSiteDbId(channelid);
	    int stationid = networkArm.getStationDbId(siteid);
	    int networkid = networkArm.getNetworkDbId(stationid);
	    NetworkAccess networkAccess = networkArm.getNetworkAccess(networkid);
	    NetworkDbObject networkDbObject = new NetworkDbObject(networkid, networkAccess);
	    ChannelDbObject[] paramChannels = new ChannelDbObject[1];
	    paramChannels[0] = channelDbObject;
	
	    Runnable work = new WaveFormArmThread(eventDbObject, 
						  eventStationSubsetter,
						  seismogramDCLocator,
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
	    // 		    System.out.println("Before Wait in getWork of ThreadPool");
	    // 		    wait();
	    // 		    System.out.println("After Wait in getWork of ThreadPool");
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
		    System.out.println("****** JOINING THE thread "+worker.getName());
		    worker.join();
		} catch(Exception e) {}
	    }
	    System.out.println("RETURNING FROM THE JOIN METHOD OF THE POOL ");
	}

	public synchronized void finished() {
	    // wait until there is no more work waiting for a thread
	    while (work != null) {
		try {
		    logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Waiting in the finished method of pool");
		    System.out.println("Before Wait in finished of Thread Pool");
		    wait();
		    System.out.println("After Wait in finished of Thread Pool");
		} catch (InterruptedException e) { }
	    }
	    logger.debug("Need not wait in the finished method of the pool");
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
	    logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Returning from the finished method");
	    //System.exit(0);
	}


	private boolean finished = false;

	private WaveFormArm waveformArm;
    }

    
    

    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();

    private ThreadPool pool;

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm = null;
    
    private Element config = null;

    private SeismogramDCLocator seismogramDCLocator= null;

    private SodExceptionListener sodExceptionListener;

    private WaveformStatusProcess waveformStatusProcess = new NullWaveformStatusProcess();

    HashMap channelDbObjectMap = new HashMap();

    HashMap eventDbObjectMap = new HashMap();

    static Category logger = 
	Category.getInstance(WaveFormArm.class.getName());
    
}
 
