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
		else if(sodElement instanceof EventChannelSubsetter) eventChannelSubsetter = (EventChannelSubsetter)sodElement;
		else if(sodElement instanceof FixedDataCenter) fixedDataCenterSubsetter = (FixedDataCenter)sodElement;
		else if(sodElement instanceof RequestGenerator) requestGeneratorSubsetter = (RequestGenerator)sodElement;
	
		else if(sodElement instanceof AvailableDataSubsetter) availableDataSubsetter = (AvailableDataSubsetter)sodElement;
		else if(sodElement instanceof WaveFormArmProcess) waveFormArmProcessSubsetter = (WaveFormArmProcess)sodElement;
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
		processEventChannelSubsetter(eventAccess,null,successfulChannels[counter]);
	    }
	}
	
    }

    /**
     * Describe <code>processEventChannelSubsetter</code> method here.
     *
     * @exception Exception if an error occurs
     */
    public void processEventChannelSubsetter(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel) throws Exception{

	if(eventChannelSubsetter.accept(eventAccess, networkAccess, channel, null)) {
	    processFixedDataCenter(eventAccess, networkAccess, channel);
	}
    }

    /**
     * Describe <code>processFixedDataCenter</code> method here.
     *
     */
    public void processFixedDataCenter(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel) throws Exception{
	DataCenter dataCenter = fixedDataCenterSubsetter.getSeismogramDC();
	if(dataCenter == null) System.out.println("****** Data Center is NULL ******");
	else System.out.println("****** Data Center is NOT NULL ******");
	processRequestGeneratorSubsetter(eventAccess, networkAccess, channel);
	
    }

    /**
     * Describe <code>processRequestGeneratorSubsetter</code> method here.
     *
     */
    public void processRequestGeneratorSubsetter(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel) throws Exception{
	System.out.println("Processing RequestGenerator");
	RequestFilter[] filters = requestGeneratorSubsetter.generateRequest(eventAccess, networkAccess, channel, null); 
	{
	   // processAvailableDataSubsetter();
	}
	
    }
    
    /**
     * Describe <code>processAvailableDataSubsetter</code> method here.
     *
     */
    public void processAvailableDataSubsetter() {

 	System.out.println("Successfully iterated through the WaveFormArm");
    }

    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();

    private EventChannelSubsetter eventChannelSubsetter = new NullEventChannelSubsetter();

    private FixedDataCenter fixedDataCenterSubsetter = null;
    
    private RequestGenerator requestGeneratorSubsetter = null;//new NullRequestGenerator();
    
    private AvailableDataSubsetter availableDataSubsetter = new NullAvailableDataSubsetter();

    private WaveFormArmProcess waveFormArmProcessSubsetter;

    private NetworkArm networkArm = null;
    
    private Element config = null;

    static Category logger = 
	Category.getInstance(WaveFormArm.class.getName());
    
}
