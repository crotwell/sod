package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.subsetter.waveFormArm.*;

import edu.sc.seis.fissuresUtil.cache.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import java.util.*;

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
    public WaveFormArm(Element config, NetworkArm networkArm, SodExceptionListener sodExceptionListener) throws Exception {
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
	pool = new ThreadPool(5);
    }
	
    /**
     * Describe <code>run</code> method here.
     *
     */
    public void run() {
	EventAccessOperations eventAccess = null;  
	try {
	    System.out.println("IN The waveform Arm Thread before POPPING");
	    int i = 0;
	    eventAccess = (EventAccessOperations)Start.getEventQueue().pop();
	    System.out.println("THE DBID OBTAINED IS @#############@#@#@@#@#@#@#@#@#@#@#@# #@#@#@# "+i);
	    logger.debug("The queue is size "+Start.getEventQueue().getLength());
	    // if(Start.getEventQueue().getLength() < 4) notifyAll();
	    while(eventAccess != null) {
		logger.debug("The name of the event is "+eventAccess.get_attributes().name);
		Channel[] successfulChannels = 
		    networkArm.getSuccessfulChannels();

		Runnable work = new WaveFormArmThread(eventAccess, 
						      eventStationSubsetter,
						      seismogramDCLocator,
						      localSeismogramArm,
						      successfulChannels, 
						      this,
						      sodExceptionListener);
			    
		pool.doWork(work);
		
		eventAccess = 
		    (EventAccessOperations)Start.getEventQueue().pop();
	    }   
	    logger.debug("CALLING THE FINISHED METHOD OF THE POOL");
	    pool.finished();
	    //   pool.join(); 
	    // logger.debug("The active count is "+Thread.activeCount());
	    //logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!MUST EXIT THE RUN METHOD THE WAVEFORMARMTHREAD NOW");
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
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)

    }

    public synchronized void signalWaveFormArm()  {
	notifyAll();
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
	    while ( ! finished) {
		logger.debug("Starting the Worker Thread");
		work.run();
		logger.debug("THe active count of thread is ");
		getThreadGroup().list();
		logger.debug("GO AND GET NEW WORK");
		work = pool.getWork();
		logger.debug("AFTER GETTING WOEK");
	    } // end of while ( ! finished)
	    logger.debug("EXITING THE RUN METHOD OF WORKER");
	}
    }

    

    class ThreadPool {

	ThreadPool(int n) {
	    for (int i=0; i<n; i++) {
		Thread t = new ThreadWorker(this);
		t.setName("mythread"+i);
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
		    wait();
		} catch (InterruptedException e) { }
	    }
	    work = workUnit;
	    notifyAll();
	}

	public synchronized Runnable getWork() {
	    logger.debug("INSIDETHE METHOD GETWORK");
	    while (work == null && ! finished) {
		try {
		    logger.debug("Waiting in the getWork METHOD");
		    wait();
		} catch (InterruptedException e) { }
	    }
	    if (finished) {
		logger.debug("finished everything so just...returning null");
		notifyAll();
		return null;
	    } // end of if (finished)
	    logger.debug("returning mywork");
	    Runnable myWork = work;
	    work = null;
	    notifyAll();
	    return myWork;
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
		    wait(10);
		} catch (InterruptedException e) { }
	    }
	    logger.debug("Need not wait in the finished method of the pool");
	    finished = true;
	    Iterator it = pool.iterator();
	    while (it.hasNext()) {
		((ThreadWorker)it.next()).finish();
	    } // end of while (it.hasNext())
	    logger.debug("Finished the calling finish method of each Thread Worker");
	 //    it = pool.iterator();
// 	    while (it.hasNext()) {
// 		notifyAll();
// 		it.next();
// 	    }
	    logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Returning from the finished method");
	    //System.exit(0);
	}


	private boolean finished = false;
    }

    

    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();

    private ThreadPool pool;

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm = null;
    
    private Element config = null;

    private SeismogramDCLocator seismogramDCLocator= null;

    private SodExceptionListener sodExceptionListener;

    static Category logger = 
	Category.getInstance(WaveFormArm.class.getName());
    
}
 
