package edu.sc.seis.sod;
import edu.sc.seis.sod.subsetter.waveformArm.*;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramProcess;
import edu.sc.seis.sod.subsetter.Subsetter;
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

    protected void processConfig(Element config)
        throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node,"waveformArm");
                if(sodElement instanceof EventChannelSubsetter) {
                    eventChannel = (EventChannelSubsetter)sodElement;
                } else if(sodElement instanceof RequestGenerator)  {
                    requestGenerator = (RequestGenerator)sodElement;
                } else if(sodElement instanceof RequestSubsetter)  {
                    request = (RequestSubsetter)sodElement;
                } else if(sodElement instanceof SeismogramDCLocator)  {
                    dcLocator = (SeismogramDCLocator)sodElement;
                } else if(sodElement instanceof AvailableDataSubsetter)  {
                    availData = (AvailableDataSubsetter)sodElement;
                } else if(sodElement instanceof LocalSeismogramSubsetter)  {
                    seisSubsetter = (LocalSeismogramSubsetter)sodElement;
                } else if(sodElement instanceof LocalSeismogramProcess) {
                    processes.add(sodElement);
                } else {
                    logger.warn("Unknown tag in LocalSeismogramArm config. " +sodElement);
                } // end of else
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)

    }

    public EventChannelSubsetter getEventChannelSubsetter() {
        return eventChannel;
    }

    public RequestGenerator getRequestGenerator() {
        return requestGenerator;
    }

    public RequestSubsetter getRequestSubsetter() {
        return request;
    }

    public AvailableDataSubsetter getAvailableDataSubsetter() {
        return availData;
    }

    public SeismogramDCLocator getSeismogramDCLocator() {
        return dcLocator;
    }

    public LocalSeismogramProcess[] getProcesses() {
        return (LocalSeismogramProcess[])processes.toArray(new LocalSeismogramProcess[0]);
    }

    public void processLocalSeismogramArm(EventChannelPair ecp){
        boolean passed;
        EventAccessOperations eventAccess = ecp.getEvent();
        Channel channel = ecp.getChannel();
        synchronized (eventChannel) {
            try {
                passed = eventChannel.accept(eventAccess,channel, ecp.getCookieJar());
            } catch (Throwable e) {
                handle(ecp, Stage.EVENT_CHANNEL_SUBSETTER, e);
                return;
            }
        }
        if( passed ) {
            ecp.update(Status.get(Stage.REQUEST_SUBSETTER, Standing.IN_PROG));
            processRequestGeneratorSubsetter(ecp);
        } else {
            logger.info("FAIL event channel");
            ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,Standing.REJECT));
        }
    }

    public void processRequestGeneratorSubsetter(EventChannelPair ecp){
        RequestFilter[] infilters;
        synchronized (requestGenerator) {
            try {
                infilters=requestGenerator.generateRequest(ecp.getEvent(),
                                                           ecp.getChannel(),
                                                          ecp.getCookieJar());
            } catch (Throwable e) {
                handle(ecp, Stage.REQUEST_SUBSETTER, e);
                return;
            }
        }
        processRequestSubsetter(ecp, infilters);
    }

    public void processRequestSubsetter(EventChannelPair ecp, RequestFilter[] infilters){
        boolean passed;
        synchronized (request) {
            try {
                passed = request.accept(ecp.getEvent(), ecp.getChannel(),
                                        infilters, ecp.getCookieJar());
            } catch (Throwable e) {
                handle(ecp, Stage.REQUEST_SUBSETTER, e);
                return;
            }
        }
        if( passed ) {
            ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.IN_PROG));
            ProxySeismogramDC dataCenter;
            synchronized(dcLocator) {
                try {
                    dataCenter = dcLocator.getSeismogramDC(ecp.getEvent(), ecp.getChannel(),
                                                           infilters, ecp.getCookieJar());
                } catch (Throwable e) {
                    handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                    return;
                }
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
                        handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                        return;
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
            ecp.update(Status.get(Stage.REQUEST_SUBSETTER, Standing.REJECT));
        }
    }


    public void processAvailableDataSubsetter(EventChannelPair ecp,
                                              ProxySeismogramDC dataCenter,
                                              RequestFilter[] infilters,
                                              RequestFilter[] outfilters){
        boolean passed;
        synchronized (availData) {
            try {
                passed = availData.accept(ecp.getEvent(), ecp.getChannel(),
                                          infilters, outfilters, ecp.getCookieJar());
            } catch (Throwable e) {
                handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                return;
            }
        }
        if( passed ) {
            ecp.update(Status.get(Stage.DATA_SUBSETTER, Standing.IN_PROG));
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
                        try {
                            localSeismograms = dataCenter.retrieve_seismograms(infilters);
                        } catch (FissuresException e) {
                            handle(ecp, Stage.DATA_SUBSETTER, e);
                            return;
                        }
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
                            handle(ecp, Stage.DATA_SUBSETTER, e);
                            return;
                        }
                    }
                }

            } else {
                logger.debug("Failed, retrieve data returned no requestFilters ");
                localSeismograms = new LocalSeismogram[0];
            } // end of else
            MicroSecondDate after = new MicroSecondDate();
            logger.info("After getting seismograms, time taken="+after.subtract(before));

            LinkedList tempForCast = new LinkedList();
            for (int i=0; i<localSeismograms.length; i++) {
                if (localSeismograms[i] == null) {
                    ecp.update(Status.get(Stage.DATA_SUBSETTER, Standing.REJECT));
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
                tempForCast.add(localSeismograms[i]);
            } // end of for (int i=0; i<localSeismograms.length; i++)
            LocalSeismogramImpl[] tempLocalSeismograms =
                (LocalSeismogramImpl[])tempForCast.toArray(new LocalSeismogramImpl[0]);
            processLocalSeismogramSubsetter(ecp, infilters, outfilters,
                                            tempLocalSeismograms);
        } else {
            logger.info("FAIL available data");
            ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                  Standing.REJECT));
        }
    }

    public void processLocalSeismogramSubsetter(EventChannelPair ecp,
                                                RequestFilter[] infilters,
                                                RequestFilter[] outfilters,
                                                LocalSeismogramImpl[] localSeismograms) {
        boolean passed;
        synchronized (seisSubsetter) {
            try {
                passed = seisSubsetter.accept(ecp.getEvent(), ecp.getChannel(),
                                              infilters, outfilters,
                                              localSeismograms, ecp.getCookieJar());
            } catch (Throwable e) {
                handle(ecp, Stage.DATA_SUBSETTER, e);
                return;
            }
        }
        if( passed ) {
            ecp.update(Status.get(Stage.PROCESSOR, Standing.IN_PROG));
            try {
                processSeismograms(ecp, infilters, outfilters, localSeismograms);
            } catch (Throwable e) {
                handle(ecp, Stage.DATA_SUBSETTER, e);
            }
        } else {
            logger.info("FAIL seismogram subsetter");
            ecp.update(Status.get(Stage.DATA_SUBSETTER, Standing.REJECT));
        }

    }

    public void processSeismograms(EventChannelPair ecp,
                                   RequestFilter[] infilters,
                                   RequestFilter[] outfilters,
                                   LocalSeismogramImpl[] localSeismograms)
        throws Exception {
        LocalSeismogramProcess processor;
        Iterator it = processes.iterator();
        while (it.hasNext()) {
            processor = (LocalSeismogramProcess)it.next();
            synchronized (processor) {
                try {
                    localSeismograms = processor.process(ecp.getEvent(),
                                                         ecp.getChannel(),
                                                         infilters,
                                                         outfilters,
                                                         localSeismograms,
                                                         ecp.getCookieJar());
                } catch (Throwable e) { handle(ecp, Stage.PROCESSOR, e); }
            }
        } // end of while (it.hasNext())
        logger.debug("finished with "+
                         ChannelIdUtil.toStringNoDates(ecp.getChannel().get_id()));
        ecp.update(Status.get(Stage.PROCESSOR, Standing.SUCCESS));
    }

    private static void handle(EventChannelPair ecp, Stage stage, Throwable t){
        if(t instanceof org.omg.CORBA.SystemException){
            ecp.update(t, Status.get(stage, Standing.CORBA_FAILURE));
        }else{
            ecp.update(t, Status.get(stage, Standing.SYSTEM_FAILURE));
        }
    }

    private EventChannelSubsetter eventChannel =new NullEventChannelSubsetter();

    private RequestGenerator requestGenerator = new NullRequestGenerator();

    private RequestSubsetter request = new NullRequestSubsetter();

    private AvailableDataSubsetter availData = new NullAvailableDataSubsetter();

    private LocalSeismogramSubsetter seisSubsetter =
        new NullLocalSeismogramSubsetter();

    private LinkedList processes = new LinkedList();

    private SeismogramDCLocator dcLocator = new NullSeismogramDCLocator();

    private static final Logger logger =Logger.getLogger(LocalSeismogramArm.class);
}// LocalSeismogramArm

