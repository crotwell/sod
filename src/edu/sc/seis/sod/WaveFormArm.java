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
public class WaveFormArm implements Runnable {

    /**
     * Creates a new <code>WaveFormArm</code> instance.
     *
     * @param config an <code>Element</code> value
     * @param networkArm a <code>NetworkArm</code> value
     */
    public WaveFormArm(Element config, NetworkArm networkArm) {
		System.out.println("WAVE FORM ARM STARTED ");
		if ( ! config.getTagName().equals("waveFormArm")) {
		    throw new IllegalArgumentException("Configuration element must be a waveFormArm tag");
		}
		System.out.println("In waveForm Arm");
		try {
		    processConfig(config);
		} catch(ConfigurationException ce) {

		    System.out.println("Configuration Exception caught while processing WaveForm Arm");
		}
		this.config = config;
		this.networkArm = networkArm;
		Thread thread = new Thread(this);
		thread.start();	
	}
	
    /**
     * Describe <code>run</code> method here.
     *
     */
    public void run() {
	
	EventAccess eventAccess = null;
	while(eventAccess == null)
	{
	    //	System.out.println("RETRIEVED THE EVENT ACCESS FROM EVENT QUEUE");
	    eventAccess = EventAccessHelper.narrow(Start.getEventQueue().pop());	
	    if(eventAccess == null);// System.out.println("EventACCESS is NULL");
	    else System.out.println("Event Access is VALID");
	    try {
		Thread.sleep(3000);	
	    } catch(Exception e){}
	  
	}
	try {
	    if(eventAccess == null){ System.out.println("EventAccess is NULL");System.exit(0);}
	    processWaveFormArm(eventAccess);
	} catch(Exception ce) {
	    ce.printStackTrace();
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
	    logger.debug(node.getNodeName());
	    if (node instanceof Element) {
		if (((Element)node).getTagName().equals("description")) {
		    // skip description element
		    continue;
		}
		Object sodElement = SodUtil.load((Element)node,"edu.sc.seis.sod.subsetter.waveFormArm");
		if(sodElement instanceof EventStationSubsetter) eventStationSubsetter = (EventStationSubsetter)sodElement;
		else if(sodElement instanceof LocalSeismogramArm) localSeismogramArm = (LocalSeismogramArm)sodElement;
	
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)

    }

    /**
     * Describe <code>processWaveFormArm</code> method here.
     *
     * @param eventAccess an <code>EventAccess</code> value
     * @exception Exception if an error occurs
     */
    public void processWaveFormArm(EventAccess eventAccess) throws Exception{

	Channel[] successfulChannels = networkArm.getSuccessfulChannels();
	
	for(int counter = 0; counter < successfulChannels.length; counter++) {
	    System.out.println("Calling accept on eventStationSubsetter");
	    if(eventStationSubsetter == null) System.out.println("NULL");
	    else System.out.println("NOT NULL");
	    if(eventStationSubsetter.accept(eventAccess, null, successfulChannels[counter].my_site.my_station, null)) {
		localSeismogramArm.processLocalSeismogramArm(eventAccess, null, successfulChannels[counter]);  
	    }
	}
	
    }

   

    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm = null;
    
    private Element config = null;

    static Category logger = 
	Category.getInstance(WaveFormArm.class.getName());
    
}
