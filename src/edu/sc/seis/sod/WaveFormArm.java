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

    private synchronized void  restoreDb() {
	//	Connection connection = Start.getWaveformQueue().getConnection(
	//	synchronized(connection) {
	int[] ids = Start.getWaveformQueue().getIds();
	System.out.println(" %%%%%%%%%%%%%%%%%%%%%%%%%%%The count of channelids to restore is "+ids.length);
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
     * Describe <code>run</code> method here.
     *
     */
    public void run() {

	EventAccessOperations eventAccess = null;  
	int eventid;
	restoreDb();
		
	//ThreadPool pool = new ThreadPool(5);
	//getThreadGroup().list();
		try {
System.out.println("IN WAVE FORM ARM BEFORE POPPING ****************************");

	    int i = 0;
	    eventid = Start.getEventQueue().pop();
	    logger.debug("The queue is size "+Start.getEventQueue().getLength());
	    // if(Start.getEventQueue().getLength() < 4) notifyAll();
	    while(eventid != -1) {
		eventAccess = Start.getEventQueue().getEventAccess(eventid);
		waveformStatusProcess.begin(eventAccess);
		
		Connection connection = Start.getWaveformQueue().getConnection();
		    // if reopen begin the transaction.
		// Start.getWaveformQueue().beginTransaction();
		    
		NetworkDbObject[] networks = networkArm.getSuccessfulNetworks();
	
		Start.getWaveformQueue().putInfo(eventid, 0);//networks.length);
		if(networks.length == 0) {
		    updateEventStatus(eventid, eventAccess);
		}
		for(int netcounter = 0; netcounter < networks.length; netcounter++) {
		    waveformStatusProcess.begin(eventAccess, networks[netcounter].getNetworkAccess());
		    StationDbObject[] stations = networkArm.getSuccessfulStations(networks[netcounter]);
		    
		    Start.getWaveformQueue().putNetworkInfo(eventid,
							    networks[netcounter].getDbId(),
							    0,///stations.length,
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
								networks[netcounter].getDbId(),
								0,//sites.length,
								new MicroSecondDate());
			if(sites.length == 0) {
			    updateStationCount(eventid, stations[stationcounter].getDbId(), eventAccess);
			}
			for(int sitecounter = 0; sitecounter < sites.length; sitecounter++) {
			    waveformStatusProcess.begin(eventAccess, sites[sitecounter].getSite());
			    ChannelDbObject[] successfulChannels = 
				networkArm.getSuccessfulChannels(networks[netcounter], sites[sitecounter]);
			    System.out.println("The length of the channels is "+successfulChannels.length);
			    Start.getWaveformQueue().putSiteInfo(eventid,
								 sites[sitecounter].getDbId(),
								 stations[stationcounter].getDbId(),
								 0,//successfulChannels.length,
								 new MicroSecondDate());
			    System.out.println("After inserting the site INFO");
			   
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
	
				    channelDbObjectMap.put( (new Integer(successfulChannels[counter].getDbId())).toString(),
							    successfulChannels[counter]);
				    Start.getWaveformQueue().push(eventid, 
								  sites[sitecounter].getDbId(),
								  successfulChannels[counter].getDbId());
				    updateReferences(eventid, successfulChannels[counter].getDbId());
				} //end of synchronization block
				// pool.doWork(work)
			    }
			}//end of for sitecounter
		    }//end of for stationcounter
		}//end of for netcounter
		 Start.getEventQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(), 
						      Status.AWAITING_FINAL_STATUS);
		 //	Start.getWaveformQueue().endTransaction();
		 eventid = 
		    Start.getEventQueue().pop();
	    }
	    logger.debug("CALLING THE FINISHED METHOD OF THE POOL");
	  	 
	    Start.getWaveformQueue().setSourceAlive(false);
	    pool.join();
	    restoreDb();
	 
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
    
    private synchronized void updateChannelCount(int eventid, int channelid, EventAccessOperations eventAccess) {
	Connection connection = Start.getWaveformQueue().getConnection();
	synchronized(connection) {
	int sitedbid = networkArm.getSiteDbId(channelid);

	
	int count = Start.getWaveformQueue().getChannelCount(eventid, sitedbid);  
	int unfinishedCount = Start.getWaveformQueue().unfinishedChannelCount(eventid, sitedbid);
	System.out.println("Channel count before is "+count+" the channelid is "+channelid+
	"eventid is  "+eventid);

	if(count == -1) { System.out.println("The coutn is -1 "); System.exit(0); }
	if(count > 0 && count > unfinishedCount) {
		Start.getWaveformQueue().decrementChannelCount(eventid, sitedbid);
	      count = Start.getWaveformQueue().getChannelCount(eventid, sitedbid);
	  if(count == -1) { System.out.println("The channel count after decr is -1 "); System.exit(0);}
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
	
    private synchronized void updateSiteCount(int eventid, int sitedbid, EventAccessOperations eventAccess) {
	
	boolean flag = false;
	int stationdbid = networkArm.getStationDbId(sitedbid);
	int count = Start.getWaveformQueue().getSiteCount(eventid, stationdbid);
	int unfinishedCount = Start.getWaveformQueue().unfinishedSiteCount(eventid, stationdbid);
	if(count == -1) { System.out.println("COuntis -1"); System.exit(0);}
	write("site count before is "+count+" the siteid is "+sitedbid
	+" eventid is "+eventid);
	if(count > 0 && count > unfinishedCount) {
		Start.getWaveformQueue().decrementSiteCount(eventid, stationdbid);
	count = Start.getWaveformQueue().getSiteCount(eventid, stationdbid);
	if(count == -1) {System.out.println("The site count after is -1 "); System.exit(0);}
	}
	write("site count after is "+Start.getWaveformQueue().getSiteCount(eventid, stationdbid)+
		"the siteid is "+sitedbid+" eventid is "+eventid);
	//  Start.getWaveformQueue().deleteSiteInfo(eventid, sitedbid);
	if(count <= 1) {
	    flag = true;
	    updateStationCount(eventid, stationdbid, eventAccess);
	}

    }
    
    private synchronized void updateStationCount(int eventid, int stationdbid, EventAccessOperations eventAccess) {

	boolean flag = false;
	int networkdbid = networkArm.getNetworkDbId(stationdbid);
	int count = Start.getWaveformQueue().getStationCount(eventid, networkdbid);
	int unfinishedCount = Start.getWaveformQueue().unfinishedStationCount(eventid, networkdbid);
	write("the station count before is "+count+" the stationid is "+stationdbid+
	" eventid is "+eventid);
	if(count == -1) {System.out.println("The COunt is -1 "); System.exit(0);}
	if(count > 0 && count > unfinishedCount) {
		Start.getWaveformQueue().decrementStationCount(eventid, networkdbid);
	count = Start.getWaveformQueue().getStationCount(eventid, networkdbid);
	if(count == -1) {System.out.println("The stationcount afrter is -1 ");System.exit(0); }
	}
	write("the station count after is "+Start.getWaveformQueue().getStationCount(eventid, networkdbid)+
		" the stationid is "+stationdbid+" evetnid is "+eventid);
	//	    Start.getWaveformQueue().deleteStationInfo(eventid, stationdbid);
	if(count <= 1) {
	    flag = true;
	    updateNetworkCount(eventid, networkdbid, eventAccess);
	}
    }

    private synchronized void updateNetworkCount(int eventid, int networkdbid, EventAccessOperations eventAccess) {
	
	boolean flag = false;
	int count = Start.getWaveformQueue().getNetworkCount(eventid);
	int unfinishedCount = Start.getWaveformQueue().unfinishedNetworkCount(eventid);
	write("the network count before is "+count+" the networkid is "+networkdbid+
	" eventid is "+eventid);
	if(count == -1) {System.out.println("The count is -1 "); System.exit(0);}
	if(count > 0 && count > unfinishedCount) {
		Start.getWaveformQueue().decrementNetworkCount(eventid);
count = Start.getWaveformQueue().getNetworkCount(eventid);
if(count == -1) {System.out.println("The net count after is -1"); System.exit(0);}
	}
	write("the network count after is "+Start.getWaveformQueue().getNetworkCount(eventid)+
		" the networkid is "+networkdbid+" evetnid is "+eventid);	
	//    Start.getWaveformQueue().deleteNetworkInfo(eventid, networkdbid);
	if(count <= 1) {
	    flag = true;
	    updateEventStatus(eventid, eventAccess);
	}
    }
    
    private synchronized void updateEventStatus(int eventid, EventAccessOperations eventAccess) {
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
		    System.out.println("******** Channnel already in the hasmamp");
		    channelDbObject = ((ChannelDbObject)channelDbObjectMap.get((new Integer(channelid)).toString()));
		    Start.getWaveformQueue().setStatus(Start.getWaveformQueue().getWaveformId(eventid, channelid),
							   Status.PROCESSING, "got channeldbobject from map");
			
		} else {
		    channelDbObject = new ChannelDbObject(channelid, networkArm.getChannel(channelid));
		}
	    } catch(Throwable ce) {
		ce.printStackTrace();
		System.exit(0);
	    }
	    setFinalStatus(eventDbObject, channelDbObject,
	    Status.PROCESSING, "still didnot even form the waveformThread Runnable");
	    if(eventid == 11 && channelid == 27) {
		System.out.println("got the needed one");
		System.out.println("The eventid is "+eventDbObject.getDbId());
		System.out.println("The channelid is "+channelDbObject.getDbId());
		//	System.exit(0);
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
		setFinalStatus(eventDbObject, channelDbObject, 
				Status.PROCESSING,
				"Exception occured ");
			System.exit(0);
	   }
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
 
