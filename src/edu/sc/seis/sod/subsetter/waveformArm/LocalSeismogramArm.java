package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * sample xml
 *<pre>
 *&lt;localSeismogramArm&gt;
 *  &lt;phaseRequest&gt;
 *      &lt;beginPhase&gt;ttp&lt;/beginPhase&gt;
 *      &lt;beginOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;-120&lt;/value&gt;
 *      &lt;/beginOffset&gt;
 *      &lt;endPhase&gt;tts&lt;/endPhase&gt;
 *      &lt;endOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;600&lt;/value&gt;
 *      &lt;/endOffset&gt;
 *  &lt;/phaseRequest&gt;
 *
 *  &lt;availableDataAND&gt;
 *      &lt;nogaps/&gt;
 *      &lt;fullCoverage/&gt;
 *  &lt;/availableDataAND&gt;
 *
 *  &lt;sacFileProcessor&gt;
 *      &lt;dataDirectory&gt;SceppEvents&lt;/dataDirectory&gt;
 *  &lt;/sacFileProcessor&gt;
 *&lt;/localSeismogramArm&gt;
 *</pre>
 */


public class LocalSeismogramArm implements Subsetter{
    public LocalSeismogramArm (Element config) throws ConfigurationException{
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
                Object sodElement = SodUtil.load((Element)node,"waveFormArm");
                if(sodElement instanceof EventChannelSubsetter) {
                    eventChannelSubsetter = (EventChannelSubsetter)sodElement;
                } else if(sodElement instanceof RequestGenerator)  {
                    requestGeneratorSubsetter = (RequestGenerator)sodElement;
                } else if(sodElement instanceof RequestSubsetter)  {
                    requestSubsetter = (RequestSubsetter)sodElement;
                } else if(sodElement instanceof SeismogramDCLocator)  {
                    seismogramDCLocator = (SeismogramDCLocator)sodElement;
                } else if(sodElement instanceof AvailableDataSubsetter)  {
                    availableDataSubsetter = (AvailableDataSubsetter)sodElement;
                } else if(sodElement instanceof LocalSeismogramSubsetter)  {
                    localSeismogramSubsetter = (LocalSeismogramSubsetter)sodElement;
                } else if(sodElement instanceof LocalSeismogramProcess) {
                    localSeisProcessList.add(sodElement);
                } else {
                    logger.warn("Unknown tag in LocalSeismogramArm config. " +sodElement);
                } // end of else
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
        
    }
    
    public void processLocalSeismogramArm(EventChannelPair ecp) throws Exception{
        EventAccessOperations eventAccess = ecp.getEvent();
        Channel channel = ecp.getChannel();
        MicroSecondDate chanBegin = new MicroSecondDate(channel.effective_time.start_time);
        MicroSecondDate chanEnd;
        if(channel.effective_time.end_time != null) {
            chanEnd = new MicroSecondDate(channel.effective_time.end_time);
        } else chanEnd = TimeUtils.future;
        if(chanEnd.before(chanBegin)) chanEnd = TimeUtils.future;
        MicroSecondDate originTime = new MicroSecondDate(eventAccess.get_preferred_origin().origin_time);
        // don't bother with channel if effective time does not
        // overlap event time
        EventEffectiveTimeOverlap eventOverlap =
            new EventEffectiveTimeOverlap(eventAccess);
        if ( ! eventOverlap.overlaps(channel)) {
            logger.info("fail "+ChannelIdUtil.toString(channel.get_id())+" doesn't everlap originTime="+originTime+" plus "+EventEffectiveTimeOverlap.getOffset()+" endTime="+chanEnd+" begin="+chanBegin);
            ecp.update("channel EffectiveTime does not overlap event",
                       EventChannelCondition.SUBSETTER_FAILED);
            return;
        } // end of if ()
        ecp.update("completedEffectiveTimeOverlaps",  EventChannelCondition.SUBSETTING);
        processEventChannelSubsetter(ecp);
        
    }
    
    public void processEventChannelSubsetter(EventChannelPair ecp) throws Exception{
        boolean passed;
        EventAccessOperations eventAccess = ecp.getEvent();
        NetworkAccess networkAccess = ecp.getNet();
        Channel channel = ecp.getChannel();
        synchronized (eventChannelSubsetter) {
            passed = eventChannelSubsetter.accept(eventAccess, networkAccess,
                                                  channel, null);
        }
        if( passed ) {
            ecp.update("Event Channel Subsetter Succeeded", EventChannelCondition.SUBSETTING);
            processRequestGeneratorSubsetter(ecp);
        } else {
            logger.info("FAIL event channel");
            ecp.update("Event Channel Subsetter Failed",  EventChannelCondition.SUBSETTER_FAILED);
        }
    }
    
    public void processRequestGeneratorSubsetter(EventChannelPair ecp)
        throws Exception {
        
        RequestFilter[] infilters;
        synchronized (requestGeneratorSubsetter) {
            infilters=requestGeneratorSubsetter.generateRequest(ecp.getEvent(),
                                                                ecp.getNet(),
                                                                ecp.getChannel(),
                                                                null);
        }
        ecp.update("Finished generating requests",EventChannelCondition.SUBSETTING);
        processRequestSubsetter(ecp, infilters);
    }
    
    public void processRequestSubsetter(EventChannelPair ecp, RequestFilter[] infilters)
        throws Exception {
        boolean passed;
        synchronized (requestSubsetter) {
            passed = requestSubsetter.accept(ecp.getEvent(),
                                             ecp.getNet(),
                                             ecp.getChannel(),
                                             infilters,
                                             null);
        }
        if( passed ) {
            ecp.update("Finished request subsetter", EventChannelCondition.SUBSETTING);
            ProxySeismogramDC dataCenter;
            synchronized(seismogramDCLocator) {
                dataCenter = seismogramDCLocator.getSeismogramDC(ecp.getEvent(),
                                                                 ecp.getNet(),
                                                                 ecp.getChannel().my_site.my_station,
                                                                 null);
            }
            RequestFilter[] outfilters = null;
            logger.debug("Trying available_data for "+ChannelIdUtil.toString(infilters[0].channel_id)+
                             " from "+infilters[0].start_time.date_time+" to "+infilters[0].end_time.date_time);
            int retries = 0;
            int MAX_RETRY = 5;
            while(retries < MAX_RETRY) {
                try {
                    logger.debug("before available_data call retries="+retries);
                    outfilters = dataCenter.available_data(infilters);
                    logger.debug("after successful available_data call retries="+retries);
                    break;
                } catch (org.omg.CORBA.SystemException e) {
                    retries++;
                    logger.debug("after failed available_data call retries="+retries+" "+e.toString());
                    if (retries < MAX_RETRY) {
                        logger.info("Caught CORBA exception, retrying..."+retries, e);
                        try {
                            Thread.sleep(1000*retries);
                        } catch(InterruptedException ex) {}
                        if (retries % 2 == 0) {
                            //force reload from name service evey other try
                            dataCenter.reset();
                        }
                    } else {
                        throw e;
                    }
                }
            }
            
            if (outfilters.length != 0) {
                logger.debug("Got available_data for "+ChannelIdUtil.toString(outfilters[0].channel_id)+
                                 " from "+outfilters[0].start_time.date_time+" to "+outfilters[0].end_time.date_time);
            } else {
                logger.debug("No available_data for "+ChannelIdUtil.toString(infilters[0].channel_id));
            }
            processAvailableDataSubsetter(ecp,dataCenter,infilters,outfilters);
        } else {
            logger.info("FAIL request subsetter");
            ecp.update("Failed in the request subsetter",  EventChannelCondition.SUBSETTER_FAILED);
        }
    }
    
    
    public void processAvailableDataSubsetter(EventChannelPair ecp,
                                              ProxySeismogramDC dataCenter,
                                              RequestFilter[] infilters,
                                              RequestFilter[] outfilters)
        throws Exception {
        boolean passed;
        synchronized (availableDataSubsetter) {
            passed = availableDataSubsetter.accept(ecp.getEvent(),
                                                   ecp.getNet(),
                                                   ecp.getChannel(),
                                                   infilters,
                                                   outfilters,
                                                   null);
        }
        if( passed ) {
            ecp.update("passed available data", EventChannelCondition.SUBSETTING);
            for (int i=0; i<infilters.length; i++) {
                logger.debug("Getting seismograms "
                                 +ChannelIdUtil.toString(infilters[i].channel_id)
                                 +" from "
                                 +infilters[i].start_time.date_time
                                 +" to "
                                 +infilters[i].end_time.date_time);
            } // end of for (int i=0; i<outFilters.length; i++)
            logger.debug("Using infilters, fix this when DMC fixes server");
            
            MicroSecondDate before = new MicroSecondDate();
            LocalSeismogram[] localSeismograms = new LocalSeismogram[0];
            if (outfilters.length != 0) {
                int retries = 0;
                int MAX_RETRY = 5;
                while(retries < MAX_RETRY) {
                    try {
                        logger.debug("before retrieve_seismograms");
                        localSeismograms = dataCenter.retrieve_seismograms(infilters);
                        logger.debug("after successful retrieve_seismograms");
                        if (localSeismograms.length > 0 && ! ChannelIdUtil.areEqual(localSeismograms[0].channel_id, infilters[0].channel_id)) {
                            // must be server error
                            logger.warn("X Channel id in returned seismogram doesn not match channelid in request. req="
                                            +ChannelIdUtil.toString(infilters[0].channel_id)
                                            +" seis="
                                            +ChannelIdUtil.toString(localSeismograms[0].channel_id));
                        }
                        break;
                    } catch (org.omg.CORBA.SystemException e) {
                        retries++;
                        logger.debug("after failed retrieve_seismograms, retries="+retries);
                        if (retries < MAX_RETRY) {
                            logger.info("Caught CORBA exception, retrying..."+retries, e);
                            try {
                                Thread.sleep(1000*retries);
                            } catch(InterruptedException ex) {}
                            if (retries % 2 == 0) {
                                // reget from Name service every other time
                                dataCenter.reset();
                            }
                        } else {
                            throw e;
                        }
                    }
                }
                
            } else {
                logger.debug("Failed, available data returned no requestFilters ");
                localSeismograms = new LocalSeismogram[0];
            } // end of else
            MicroSecondDate after = new MicroSecondDate();
            logger.info("After getting seismograms, time taken="+after.subtract(before));
            
            for (int i=0; i<localSeismograms.length; i++) {
                if (localSeismograms[i] == null) {
                    ecp.update("Failed due to malformed(null) seismograms being returned",  EventChannelCondition.FAILURE);
                    logger.error("Got null in seismogram array "+ChannelIdUtil.toString(ecp.getChannel().get_id()));
                    return;
                }
                Channel ecpChan = ecp.getChannel();
                if ( ! ChannelIdUtil.areEqual(localSeismograms[i].channel_id, ecpChan.get_id())) {
                    // must be server error
                    logger.warn("Channel id in returned seismogram doesn not match channelid in request. req="
                                    +ChannelIdUtil.toString(ecpChan.get_id())
                                    +" seis="
                                    +ChannelIdUtil.toString(localSeismograms[i].channel_id));
                    // fix seis with original id
                    localSeismograms[i].channel_id = ecpChan.get_id();
                } // end of if ()
            } // end of for (int i=0; i<localSeismograms.length; i++)
            
            processLocalSeismogramSubsetter(ecp, infilters, outfilters,
                                            localSeismograms);
        } else {
            logger.info("FAIL available data");
            ecp.update("No available data",  EventChannelCondition.NO_AVAILABLE_DATA);
        }
    }
    
    public void processLocalSeismogramSubsetter(EventChannelPair ecp,
                                                RequestFilter[] infilters,
                                                RequestFilter[] outfilters,
                                                LocalSeismogram[] localSeismograms) throws Exception {
        boolean passed;
        synchronized (localSeismogramSubsetter) {
            passed = localSeismogramSubsetter.accept(ecp.getEvent(),
                                                     ecp.getNet(),
                                                     ecp.getChannel(),
                                                     infilters,
                                                     outfilters,
                                                     localSeismograms,
                                                     null);
        }
        if( passed ) {
            ecp.update("passed local seismogram subsetter", EventChannelCondition.SUBSETTER_PASSED);
            processSeismograms(ecp, infilters, outfilters, localSeismograms);
        } else {
            logger.info("FAIL seismogram subsetter");
            ecp.update("failed local seismogram subsetter",  EventChannelCondition.SUBSETTER_FAILED);
        }
        
    }
    
    public void processSeismograms(EventChannelPair ecp,
                                   RequestFilter[] infilters,
                                   RequestFilter[] outfilters,
                                   LocalSeismogram[] localSeismograms)
        throws Exception {
        LocalSeismogramProcess processor;
        Iterator it = localSeisProcessList.iterator();
        while (it.hasNext()) {
            processor = (LocalSeismogramProcess)it.next();
            synchronized (processor) {
                localSeismograms = processor.process(ecp.getEvent(),
                                                     ecp.getNet(),
                                                     ecp.getChannel(),
                                                     infilters,
                                                     outfilters,
                                                     localSeismograms,
                                                     null);
            }
        } // end of while (it.hasNext())
        logger.debug("finished with "+
                         ChannelIdUtil.toStringNoDates(ecp.getChannel().get_id()));
        ecp.update("passesd all subsetters",  EventChannelCondition.SUCCESS);
    }
    
    private EventChannelSubsetter eventChannelSubsetter =
        new NullEventChannelSubsetter();
    
    private RequestGenerator requestGeneratorSubsetter =
        new NullRequestGenerator();
    
    private RequestSubsetter requestSubsetter = new NullRequestSubsetter();
    
    private AvailableDataSubsetter availableDataSubsetter =
        new NullAvailableDataSubsetter();
    
    private LocalSeismogramSubsetter localSeismogramSubsetter =
        new NullLocalSeismogramSubsetter();
    
    private LinkedList localSeisProcessList = new LinkedList();
    
    private SeismogramDCLocator seismogramDCLocator=
        new NullSeismogramDCLocator();
    
    private static Logger logger =Logger.getLogger(LocalSeismogramArm.class);
    
}// LocalSeismogramArm

