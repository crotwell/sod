package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.subsetter.waveFormArm.*;

import edu.sc.seis.fissuresUtil.cache.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

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
		if ( ! config.getTagName().equals("waveFormArm")) {
		    throw new IllegalArgumentException("Configuration element must be a waveFormArm tag");
		}
		//System.out.println("In waveForm Arm");
		processConfig(config);

		this.config = config;
		this.networkArm = networkArm;
		addSodExceptionListener(sodExceptionListener);
		this.sodExceptionListener = sodExceptionListener;

    }
	
    /**
     * Describe <code>run</code> method here.
     *
     */
    public void run() {
	EventAccessOperations eventAccess = null;  
	try {
	    eventAccess = (EventAccessOperations)Start.getEventQueue().pop();
	    logger.debug("The queue is size "+Start.getEventQueue().getLength());
	    while(eventAccess != null) {
		logger.debug("The name of the event is "+eventAccess.get_attributes().name);
		Channel[] successfulChannels = 
		    networkArm.getSuccessfulChannels();
		if(createNewThread()) {
		    Thread thread = new Thread(new WaveFormArmThread(eventAccess, 
								     eventStationSubsetter,
								     fixedDataCenterSubsetter,
								     localSeismogramArm,
								     successfulChannels, this,
								     sodExceptionListener));
			    
		    thread.start();
		}
		eventAccess = 
		    (EventAccessOperations)Start.getEventQueue().pop();
	    }   

	} catch(Exception e) {

		e.printStackTrace();
		notifyListeners(this, e);
	}
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
                else if(sodElement instanceof FixedDataCenter) fixedDataCenterSubsetter = (FixedDataCenter)sodElement;	
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)

    }

   
    public synchronized boolean createNewThread() throws Exception{
	
	while(Thread.activeCount() > 6 ) {
	    
	    wait();
	   
	}
	return true;
    }


    public synchronized void signalWaveFormArm()  {
	
	notifyAll();
    }

    

    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();


    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm = null;
    
    private Element config = null;

    private FixedDataCenter fixedDataCenterSubsetter= null;

    private SodExceptionListener sodExceptionListener;

    static Category logger = 
	Category.getInstance(WaveFormArm.class.getName());
    
}
 
