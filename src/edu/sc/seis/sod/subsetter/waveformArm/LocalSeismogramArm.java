package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.database.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;
import org.apache.log4j.*;
import java.util.*;

/**
 * sample xml
 *<pre>
 *&lt;localSeismogramArm&gt;
 *	&lt;phaseRequest&gt;
 *		&lt;beginPhase&gt;ttp&lt;/beginPhase&gt;
 *		&lt;beginOffset&gt;
 *			&lt;unit&gt;SECOND&lt;/unit&gt;
 *			&lt;value&gt;-120&lt;/value&gt;
 *		&lt;/beginOffset&gt;
 *		&lt;endPhase&gt;tts&lt;/endPhase&gt;
 *		&lt;endOffset&gt;
 *			&lt;unit&gt;SECOND&lt;/unit&gt;
 *			&lt;value&gt;600&lt;/value&gt;
 *		&lt;/endOffset&gt;
 *	&lt;/phaseRequest&gt; 
 *
 *	&lt;availableDataAND&gt;
 *		&lt;nogaps/&gt;
 *		&lt;fullCoverage/&gt;
 *	&lt;/availableDataAND&gt; 
 *
 *	&lt;sacFileProcessor&gt;
 *		&lt;dataDirectory&gt;SceppEvents&lt;/dataDirectory&gt;
 *	&lt;/sacFileProcessor&gt;
 *&lt;/localSeismogramArm&gt;
 *</pre>
 */


public class LocalSeismogramArm implements Subsetter{
    public LocalSeismogramArm (Element config) throws ConfigurationException{
	if ( ! config.getTagName().equals("localSeismogramArm")) {
	    throw new IllegalArgumentException("Configuration element must be a localSeismogramArm tag");
	}
    	processConfig(config);
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
		if(sodElement instanceof EventChannelSubsetter) eventChannelSubsetter = (EventChannelSubsetter)sodElement;
		else if(sodElement instanceof RequestGenerator) requestGeneratorSubsetter = (RequestGenerator)sodElement;
	
		else if(sodElement instanceof AvailableDataSubsetter) availableDataSubsetter = (AvailableDataSubsetter)sodElement;
		else if(sodElement instanceof LocalSeismogramSubsetter) localSeismogramSubsetter = (LocalSeismogramSubsetter)sodElement;
		else if(sodElement instanceof LocalSeismogramProcess) {
		    localSeisProcessList.add(sodElement);
		} else {
		    logger.warn("Unknown tag in LocalSeismogramArm config. "
				+sodElement);
		} // end of else
		

	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)

    }

    /**
     * Starts the processing of the localSeismogramArm. 
     */

    public void processLocalSeismogramArm(EventDbObject eventDbObject, 
					  NetworkDbObject networkDbObject, 
					  ChannelDbObject channelDbObject, 

					  DataCenter dataCenter,
					  WaveFormArm waveformArm) throws Exception{
	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
	Channel channel = channelDbObject.getChannel();
	MicroSecondDate chanBegin = new MicroSecondDate(channel.effective_time.start_time);
	MicroSecondDate chanEnd;
	if(channel.effective_time.end_time != null) {
	    chanEnd = new MicroSecondDate(channel.effective_time.end_time);
	} else {
	    chanEnd = TimeUtils.future;
	}
	if(chanEnd.before(chanBegin)) {
	    chanEnd = TimeUtils.future;
	}

	MicroSecondDate originTime = new MicroSecondDate(eventAccess.get_preferred_origin().origin_time);

	TimeInterval day = new TimeInterval(1, UnitImpl.DAY);
	logger.info("channelbeginTime is "+chanBegin);
	logger.info("channelendTime is "+chanEnd);
	logger.info("originTime is "+originTime);
	logger.info("originTime incr is "+ originTime.add(day));
						 
	if (chanBegin.after(originTime.add(day))
	    || chanEnd.before(originTime)) {
	    // channel doesn't overlap origin
	    logger.info("fail "+ChannelIdUtil.toString(channel.get_id())+" doesn't everlap originTime="+originTime+" endTime="+chanEnd+" begin="+chanBegin);
	    waveformArm.setFinalStatus(eventDbObject,
				       channelDbObject,
				       Status.COMPLETE_REJECT,
				       "channel EffectiveTime doesnot overlap event");
	    return;
	}
	waveformArm.setFinalStatus(eventDbObject,
				   channelDbObject,
				   Status.PROCESSING,
				   "completedEffectiveTimeOverlaps");
	processEventChannelSubsetter(eventDbObject, 
				     networkDbObject, 
				     channelDbObject, 
				     dataCenter,
				     waveformArm);
	
    }

    /**
     * handles eventChannelSubsetter
     *
     * @exception Exception if an error occurs
     */
    public void processEventChannelSubsetter(EventDbObject eventDbObject,
					     NetworkDbObject networkDbObject, 
					     ChannelDbObject channelDbObject,
					     DataCenter dataCenter,
					     WaveFormArm waveformArm) throws Exception{

	boolean b;
	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
	Channel channel = channelDbObject.getChannel();
	
	synchronized (eventChannelSubsetter) {
	    b = eventChannelSubsetter.accept(eventAccess, 
					     networkAccess, 
					     channel, 
					     null);
	
	}
	if( b ) {
	    waveformArm.setFinalStatus(eventDbObject,
				       channelDbObject,
				       Status.PROCESSING,
				       "EventChannelSubsetterSucceeded");
	    processRequestGeneratorSubsetter(eventDbObject, 
					     networkDbObject, 
					     channelDbObject, 
					     dataCenter,
					     waveformArm
					     );
	} else {
	    waveformArm.setFinalStatus(eventDbObject,
				       channelDbObject,
				       Status.COMPLETE_REJECT,
				       "EventChannelSubsetterfailed");
	}
    }


    /**
     * handles requestGenerator Subsetter.
     *
     */
    public void processRequestGeneratorSubsetter(EventDbObject eventDbObject, 
						 NetworkDbObject networkDbObject, 
						 ChannelDbObject channelDbObject, 
						 DataCenter dataCenter,
						 WaveFormArm waveformArm) 
	throws Exception
    {
	RequestFilter[] infilters;
	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
	Channel channel = channelDbObject.getChannel();
	synchronized (requestGeneratorSubsetter) {
	    infilters = 
		requestGeneratorSubsetter.generateRequest(eventAccess, 
							  networkAccess, 
							  channel, 
							  null);
	}
 
	logger.debug("BEFORE getting seismograms "+infilters.length);
	for (int i=0; i<infilters.length; i++) {
	    logger.debug("Getting seismograms "
			 +ChannelIdUtil.toString(infilters[i].channel_id)
			 +" from "
			 +infilters[i].start_time.date_time
			 +" to "
			 +infilters[i].end_time.date_time);
	} // end of for (int i=0; i<outFilters.length; i++)

	RequestFilter[] outfilters = dataCenter.available_data(infilters); 
	waveformArm.setFinalStatus(eventDbObject,
				   channelDbObject,
				   Status.PROCESSING,
				   "requestgeneratorSubsettterCompleted");
	processAvailableDataSubsetter(eventDbObject, 
				      networkDbObject, 
				      channelDbObject, 
				      dataCenter, 
				      infilters, 
				      outfilters,
				      waveformArm);
    }
    
    /**
     * handles availableDataSubsetter
     *
     */
    public void processAvailableDataSubsetter(EventDbObject eventDbObject, 
					      NetworkDbObject networkDbObject, 
					      ChannelDbObject channelDbObject,
					      DataCenter dataCenter,
					      RequestFilter[] infilters,
					      RequestFilter[] outfilters,
					      WaveFormArm waveformArm)
	throws Exception
    {
	boolean b;
	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
	Channel channel = channelDbObject.getChannel();
	
	synchronized (availableDataSubsetter) {
	    b = availableDataSubsetter.accept(eventAccess, 
					      networkAccess, 
					      channel, 
					      infilters, 
					      outfilters, 
					      null);
	}
	if( b ) {
	    waveformArm.setFinalStatus(eventDbObject,
				       channelDbObject,
				       Status.PROCESSING,
				       "availableDataSubsetterCompleted");
	    logger.debug("Using infilters, fix this when DMC fixes server");
	    
	    MicroSecondDate before = new MicroSecondDate();
	    LocalSeismogram[] localSeismograms;
	    if (outfilters.length != 0) {
	//	localSeismograms = dataCenter.retrieve_seismograms(infilters);
		localSeismograms = new LocalSeismogram[0];
	    } else {
		localSeismograms = new LocalSeismogram[0];
	    } // end of else
	    
	    
	    MicroSecondDate after = new MicroSecondDate();
	    logger.debug("After getting seismograms "+after.subtract(before));
	    logger.debug("Using infilters, fix this when DMC fixes server");
	    

	    for (int i=0; i<localSeismograms.length; i++) {
		if (localSeismograms[i] == null) {
		    waveformArm.setFinalStatus(eventDbObject,
					       channelDbObject,
					       Status.COMPLETE_REJECT,
					       "rejected as the seismogram array Contained NULL entried");
		    logger.error("Got null in seismogram array "+ChannelIdUtil.toString(channel.get_id()));
		    return;
		}
		if ( ! ChannelIdUtil.areEqual(localSeismograms[i].channel_id, channel.get_id())) {
		    // must be server error
		    logger.debug("Channel id in returned seismogram doesn not match channelid in request. req="
				 +ChannelIdUtil.toString(channel.get_id())
				 +" seis="
				 +ChannelIdUtil.toString(localSeismograms[i].channel_id));
		    // fix seis with original id
		    localSeismograms[i].channel_id = channel.get_id();
		} // end of if ()
		
	    } // end of for (int i=0; i<localSeismograms.length; i++)
	    
	    processLocalSeismogramSubsetter(eventDbObject, 
					    networkDbObject, 
					    channelDbObject, 
					    infilters, 
					    outfilters, 
					    localSeismograms,
					    waveformArm);
	} else {
	    waveformArm.setFinalStatus(eventDbObject,
				       channelDbObject,
				       Status.COMPLETE_REJECT,
				       "AvailableDataSubsetterFailed");
	}
    }

    /**
     * handles LocalSeismogramSubsetter.
     */
    
    public void processLocalSeismogramSubsetter	(EventDbObject eventDbObject, 
						 NetworkDbObject networkDbObject, 
						 ChannelDbObject channelDbObject, 
						 RequestFilter[] infilters, 
						 RequestFilter[] outfilters, 
						 LocalSeismogram[] localSeismograms,
						 WaveFormArm waveformArm) throws Exception { 
	

	logger.debug("Using infilters, fix this when DMC fixes server");
	    
	boolean b;
	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
	Channel channel= channelDbObject.getChannel();
	synchronized (localSeismogramSubsetter) {
	    b = localSeismogramSubsetter.accept(eventAccess, 
						networkAccess, 
						channel, 
						infilters, 
						outfilters, 
						localSeismograms, 
						null);
	}
	if( b ) {
	    waveformArm.setFinalStatus(eventDbObject,
				       channelDbObject,
				       Status.PROCESSING,
				       "localSeismogramSubsetterAccepted");
	    processSeismograms(eventDbObject, 
			       networkDbObject, 
			       channelDbObject, 
			       infilters, 
			       outfilters, 
			       localSeismograms,
			       waveformArm);
	} else {
	    waveformArm.setFinalStatus(eventDbObject,
				       channelDbObject,
				       Status.COMPLETE_REJECT,
				       "LocalSeismogramSubsetterFailed");
	}
	    
    }

    /**
     * processes the seismograms obtained from the seismogram server.
     */
    
    public void processSeismograms(EventDbObject eventDbObject, 
				   NetworkDbObject networkDbObject, 
				   ChannelDbObject channelDbObject, 
				   RequestFilter[] infilters, 
				   RequestFilter[] outfilters, 
				   LocalSeismogram[] localSeismograms,
				   WaveFormArm waveformArm) 
	throws Exception 
    {
	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	NetworkAccess networkAccess = networkDbObject.getNetworkAccess();
	Channel channel = channelDbObject.getChannel();
	waveformArm.setFinalStatus(eventDbObject,
				   channelDbObject,
				   Status.PROCESSING,
				   "before waveformArm Processing");
	LocalSeismogramProcess processor;
	Iterator it = localSeisProcessList.iterator();
	while (it.hasNext()) {
	    processor = (LocalSeismogramProcess)it.next();
	
	    synchronized (processor) {
		localSeismograms =
		    processor.process(eventAccess, 
				      networkAccess, 
				      channel, 
				      infilters, 
				      outfilters, 
				      localSeismograms, 
				      null);
	    }
	} // end of while (it.hasNext())
	logger.debug("finished with "+
		     ChannelIdUtil.toStringNoDates(channel.get_id()));
	waveformArm.setFinalStatus(eventDbObject,
				   channelDbObject,
				   Status.COMPLETE_SUCCESS,
				   "successful");
    }


    private EventChannelSubsetter eventChannelSubsetter = 
	new NullEventChannelSubsetter();
    
    private RequestGenerator requestGeneratorSubsetter = 
	new NullRequestGenerator();
    
    private AvailableDataSubsetter availableDataSubsetter = 
	new NullAvailableDataSubsetter();

    private LocalSeismogramSubsetter localSeismogramSubsetter = 
	new NullLocalSeismogramSubsetter();

    private LinkedList localSeisProcessList = 
	new LinkedList();

   
    
    static Category logger = 
	Category.getInstance(LocalSeismogramArm.class.getName());
    
}// LocalSeismogramArm
