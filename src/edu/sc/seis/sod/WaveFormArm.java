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

public class WaveFormArm implements Runnable {

	public WaveFormArm(Element config) {
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
		
		Thread thread = new Thread(this);
		thread.start();	
	}
	
    public void run() {
	while(true){
	    //	System.out.println("RETRIEVED THE EVENT ACCESS FROM EVENT QUEUE");
	    try {
		Thread.sleep(3000);	
	    } catch(Exception e){}
	    EventAccess eventAccess = EventAccessHelper.narrow(Start.getEventQueue().pop());	
	    if(eventAccess == null);// System.out.println("EventACCESS is NULL");
	    else System.out.println("Event Access is VALID");
	}
    }

    protected void processConfig(Element config) 
	throws ConfigurationException {

	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    logger.debug(node.getNodeName());
	    if (node instanceof Element) {
		Object sodElement = SodUtil.load((Element)node,"edu.sc.seis.sod.subsetter.waveFormArm");
		if(sodElement instanceof EventStationSubsetter) eventStationSubsetter = (EventStationSubsetter)sodElement;
		else if(sodElement instanceof EventChannelSubsetter) eventChannelSubsetter = (EventChannelSubsetter)sodElement;
		else if(sodElement instanceof FixedDataCenter) fixedDataCenterSubsetter = (FixedDataCenter)sodElement;
		else if(sodElement instanceof PhaseRequestSubsetter) phaseRequestSubsetter = (PhaseRequestSubsetter)sodElement;
		else if(sodElement instanceof AvailableDataSubsetter) availableDataSubsetter = (AvailableDataSubsetter)sodElement;
		else if(sodElement instanceof WaveFormArmProcess) waveFormArmProcessSubsetter = (WaveFormArmProcess)sodElement;
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)
	processWaveFormArm();	
    }

    public void processWaveFormArm() {

	if(eventStationSubsetter.accept(null, null, null)) {
	    processEventChannelSubsetter();
	}
	
    }

    public void processEventChannelSubsetter() {

	if(eventChannelSubsetter.accept(null, null, null)) {
	    processFixedDataCenter();
	}
    }

    public void processFixedDataCenter() {
	DataCenter dataCenter = fixedDataCenterSubsetter.getSeismogramDC();
	if(dataCenter == null) System.out.println("****** Data Center is NULL ******");
	else System.out.println("****** Data Center is NOT NULL ******");
	processPhaseRequestSubsetter();
	
    }

    public void processPhaseRequestSubsetter() {

	if(phaseRequestSubsetter.accept(null)) {
	    processAvailableDataSubsetter();
	}
	
    }
    
    public void processAvailableDataSubsetter() {

 	System.out.println("Successfully iterated through the WaveFormArm");
    }

    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();

    private EventChannelSubsetter eventChannelSubsetter = new NullEventChannelSubsetter();

    private FixedDataCenter fixedDataCenterSubsetter = null;
    
    private PhaseRequestSubsetter phaseRequestSubsetter = new NullPhaseRequestSubsetter();
    
    private AvailableDataSubsetter availableDataSubsetter = new NullAvailableDataSubsetter();

    private WaveFormArmProcess waveFormArmProcessSubsetter;

    static Category logger = 
	Category.getInstance(WaveFormArm.class.getName());
    
}
