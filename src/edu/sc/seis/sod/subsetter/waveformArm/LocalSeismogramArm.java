package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;
import org.apache.log4j.*;

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
		//else if(sodElement instanceof FixedDataCenter) fixedDataCenterSubsetter = (FixedDataCenter)sodElement;
		else if(sodElement instanceof RequestGenerator) requestGeneratorSubsetter = (RequestGenerator)sodElement;
	
		else if(sodElement instanceof AvailableDataSubsetter) availableDataSubsetter = (AvailableDataSubsetter)sodElement;
		else if(sodElement instanceof LocalSeismogramSubsetter) localSeismogramSubsetter = (LocalSeismogramSubsetter)sodElement;
		else if(sodElement instanceof LocalSeismogramProcess) waveFormArmProcessSubsetter = (LocalSeismogramProcess)sodElement;
	    } // end of if (node instanceof Element)
	} // end of for (int i=0; i<children.getSize(); i++)

    }

    public void processLocalSeismogramArm(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel, DataCenter
    dataCenter) throws Exception{
	processEventChannelSubsetter(eventAccess, networkAccess, channel, dataCenter);
	
    }

    /**
     * Describe <code>processEventChannelSubsetter</code> method here.
     *
     * @exception Exception if an error occurs
     */
    public void processEventChannelSubsetter(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel,
    DataCenter dataCenter) throws Exception{

	if(eventChannelSubsetter.accept(eventAccess, networkAccess, channel, null)) {
	    //processFixedDataCenter(eventAccess, networkAccess, channel);
	     processRequestGeneratorSubsetter(eventAccess, 
					      networkAccess, 
					      channel, 
					      dataCenter);
	}
    }

    /**
     * Describe <code>processFixedDataCenter</code> method here.
     *
    
    public void processFixedDataCenter(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel) throws Exception{
	DataCenter dataCenter = fixedDataCenterSubsetter.getSeismogramDC();
	processRequestGeneratorSubsetter(eventAccess, networkAccess, channel, dataCenter);
	
    }*/

    /**
     * Describe <code>processRequestGeneratorSubsetter</code> method here.
     *
     */
    public void processRequestGeneratorSubsetter(EventAccess eventAccess, 
						 NetworkAccess networkAccess, 
						 Channel channel, 
						 DataCenter dataCenter) 
	throws Exception
    {
	RequestFilter[] infilters 
	    = requestGeneratorSubsetter.generateRequest(eventAccess, 
							networkAccess, 
							channel, 
							null); 
	logger.debug("BEFORE getting seismograms "+infilters.length);
	for (int i=0; i<infilters.length; i++) {
	    logger.debug("Getting seismograms "+ChannelIdUtil.toString(infilters[i].channel_id)+" from "+infilters[i].start_time.date_time+" to "+infilters[i].end_time.date_time);
	} // end of for (int i=0; i<outFilters.length; i++)

	RequestFilter[] outfilters = dataCenter.available_data(infilters); 

	    processAvailableDataSubsetter(eventAccess, networkAccess, channel, dataCenter, infilters, outfilters);

// 	if (outfilters.length != 0) {
// 	    processAvailableDataSubsetter(eventAccess, networkAccess, channel, dataCenter, infilters, outfilters);
// 	} else {
// 	    logger.debug("available data returned no filters for "+ChannelIdUtil.toString(channel.get_id()));
// 	} // end of else
	
	
    }
    
    /**
     * Describe <code>processAvailableDataSubsetter</code> method here.
     *
     */
    public void processAvailableDataSubsetter(EventAccess eventAccess, 
					      NetworkAccess networkAccess, 
					      Channel channel,
					      DataCenter dataCenter,
					      RequestFilter[] infilters,
					      RequestFilter[] outfilters)
	throws Exception
    {
	if(availableDataSubsetter.accept(eventAccess, 
					 networkAccess, 
					 channel, 
					 infilters, 
					 outfilters, 
					 null)) {
	    logger.debug("Using infilters, fix this when DMC fixes server");
	    
	    MicroSecondDate before = new MicroSecondDate();
	    LocalSeismogram[] localSeismograms = dataCenter.retrieve_seismograms(infilters);
	    MicroSecondDate after = new MicroSecondDate();
	    logger.debug("After getting seismograms "+after.subtract(before));
	    processLocalSeismogramSubsetter(eventAccess, networkAccess, channel, infilters, outfilters, localSeismograms);
	}
    }
    
    public void processLocalSeismogramSubsetter
	(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel, RequestFilter[] infilters, RequestFilter[] outfilters, LocalSeismogram[] localSeismograms) throws Exception{ 
	
	if(localSeismogramSubsetter.accept(eventAccess, networkAccess, channel, infilters, outfilters, localSeismograms, null)) {

	    processSeismograms(eventAccess, networkAccess, channel, infilters, outfilters, localSeismograms);

	}
	    
    }
    
    public void processSeismograms	
	(EventAccess eventAccess, NetworkAccess networkAccess, Channel channel, RequestFilter[] infilters, RequestFilter[] outfilters, LocalSeismogram[] localSeismograms) throws Exception {

	waveFormArmProcessSubsetter.process(eventAccess, networkAccess, channel, infilters, outfilters, localSeismograms, null);
    }


    private EventChannelSubsetter eventChannelSubsetter = new NullEventChannelSubsetter();
    
    private FixedDataCenter fixedDataCenterSubsetter = null;
    
    private RequestGenerator requestGeneratorSubsetter = new NullRequestGenerator();
    
    private AvailableDataSubsetter availableDataSubsetter = new NullAvailableDataSubsetter();

    private LocalSeismogramSubsetter localSeismogramSubsetter = new NullLocalSeismogramSubsetter();

    private LocalSeismogramProcess waveFormArmProcessSubsetter = new NullWaveformProcess();
    
    static Category logger = 
	Category.getInstance(LocalSeismogramArm.class.getName());
    
}// LocalSeismogramArm
