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
    }
	
    /**
     * Describe <code>run</code> method here.
     *
     */
    public void run() {
	Channel[] successfulChannels = networkArm.getSuccessfulChannels();
	EventAccess eventAccess = null;  
	do
	{
	  
	    eventAccess = EventAccessHelper.narrow(Start.getEventQueue().pop());	
	    if(eventAccess == null);// System.out.println("EventACCESS is NULL");
	    
	    else System.out.println("Event Access is VALID");
	    
	    if(eventAccess != null) {
		Thread thread = new Thread(new WaveFormArmThread(eventAccess, 
							     eventStationSubsetter, 
								 localSeismogramArm,
								 successfulChannels));
		thread.start();
	    }
	    
	    //	System.out.println("RETRIEVED THE EVENT ACCESS FROM EVENT QUEUE");
	    
	}while(eventAccess != null);

	
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

   

   

    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm = null;
    
    private Element config = null;

    static Category logger = 
	Category.getInstance(WaveFormArm.class.getName());
    
}
