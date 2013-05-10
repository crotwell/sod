package edu.sc.seis.sod;

import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.time.SortTool;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.WaveformResult;
import edu.sc.seis.sod.source.seismogram.DataCenterSource;
import edu.sc.seis.sod.source.seismogram.SeismogramSource;
import edu.sc.seis.sod.source.seismogram.SeismogramSourceException;
import edu.sc.seis.sod.source.seismogram.SeismogramSourceLocator;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.PassEventChannel;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.request.PassRequest;
import edu.sc.seis.sod.subsetter.request.RequestSubsetter;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;

public class LocalSeismogramArm extends AbstractWaveformRecipe implements Subsetter {

    public LocalSeismogramArm(Element config) throws ConfigurationException {
        processConfig(config);
        logger.info("EventStation: "+getEventStationSubsetter().getClass().getName());
        logger.info("EventChannel: "+getEventChannelSubsetter().getClass().getName());
        logger.info("RequestGenerator: "+getRequestGenerator().getClass().getName());
        logger.info("RequestSubsetter: "+getRequestSubsetter().getClass().getName());
        logger.info("SeismogramSourceLocator: "+getSeismogramDCLocator().getClass().getName());
        logger.info("AvailableDataSubsetter: "+getAvailableDataSubsetter().getClass().getName());
        WaveformProcess[] p = getProcesses();
        for (WaveformProcess process : p) {
            logger.info("WaveformProcess: "+process.getClass().getName());
        }
    }

    public void handle(Element el) throws ConfigurationException {
        Object sodObject = SodUtil.load(el, PACKAGES);
        if(sodObject instanceof EventStationSubsetter) {
            eventStation = (EventStationSubsetter)sodObject;
        } else if(sodObject instanceof WaveformMonitor) {
            addStatusMonitor((WaveformMonitor)sodObject);
        } else if(sodObject instanceof EventChannelSubsetter) {
            eventChannel = (EventChannelSubsetter)sodObject;
        } else if(sodObject instanceof RequestGenerator) {
            requestGenerator = (RequestGenerator)sodObject;
        } else if(sodObject instanceof RequestSubsetter) {
            request = (RequestSubsetter)sodObject;
        } else if(sodObject instanceof SeismogramSourceLocator) {
            dcLocator = (SeismogramSourceLocator)sodObject;
        } else if(sodObject instanceof AvailableDataSubsetter) {
            availData = (AvailableDataSubsetter)sodObject;
        } else if(sodObject instanceof WaveformProcess) {
            add((WaveformProcess)sodObject);
        } else {
            throw new ConfigurationException("Unknown tag in LocalSeismogramArm config. " + el.getLocalName());
        } // end of else
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

    public SeismogramSourceLocator getSeismogramDCLocator() {
        return dcLocator;
    }

    public WaveformProcess[] getProcesses() {
        return (WaveformProcess[])processes.toArray(new WaveformProcess[0]);
    }

    public void add(WaveformProcess proc) {
        if(proc == null) {
            throw new IllegalArgumentException("WaveformProcess cannot be null");
        }
        synchronized(this) {
            if(processes == null) {
                processes = new LinkedList<WaveformProcess>();
            }
        }
        processes.add(proc);
    }

    public void processLocalSeismogramArm(EventChannelPair ecp) {
        logger.debug("Begin ECP: " + ecp.toString());
        logger.debug("      ESP: " + ecp.getEsp().toString());
        ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.IN_PROG));
        StringTree passed;
        CacheEvent eventAccess = ecp.getEvent();
        ChannelImpl channel = ecp.getChannel();
        synchronized(eventChannel) {
            try {
                passed = eventChannel.accept(eventAccess, channel, new CookieJar(ecp,
                                                                                 ecp.getEsp().getCookies(),
                                                                                 ecp.getCookies()));
            } catch(Throwable e) {
                MotionVectorArm.handle(ecp, Stage.EVENT_CHANNEL_SUBSETTER, e, null, "");
                return;
            }
        }
        if(passed.isSuccess()) {
            processRequestGeneratorSubsetter(ecp);
        } else {
            ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.REJECT));
            failLogger.info(ecp + ": " + passed.toString());
        }
    }

    public void processRequestGeneratorSubsetter(EventChannelPair ecp) {
        RequestFilter[] infilters;
        synchronized(requestGenerator) {
            try {
                infilters = requestGenerator.generateRequest(ecp.getEvent(), ecp.getChannel(), ecp.getCookieJar());
            } catch(Throwable e) {
                MotionVectorArm.handle(ecp, Stage.REQUEST_SUBSETTER, e, null, "");
                return;
            }
        }
        processRequestSubsetter(ecp, SortTool.byBeginTimeAscending(infilters));
    }

    private boolean firstRequest;

    public void processRequestSubsetter(EventChannelPair ecp, RequestFilter[] infilters) {
        StringTree passed;
        synchronized(request) {
            try {
                passed = request.accept(ecp.getEvent(), ecp.getChannel(), infilters, ecp.getCookieJar());
            } catch(Throwable e) {
                MotionVectorArm.handle(ecp, Stage.REQUEST_SUBSETTER, e, null, requestToString(infilters, null));
                return;
            }
        }
        if(getProcesses().length == 0 
                && getAvailableDataSubsetter().equals(defaultAvailableDataSubsetter)) {
            if(firstRequest) {
                firstRequest = false;
                logger.info("No seismogram processors have been set, so no data is being requested.  If you're only generating BreqFast requests, this is fine.  Otherwise, it's probably an error.");
            }
            ecp.update(Status.get(Stage.PROCESSOR, Standing.SUCCESS));
            return;
        }
        if(passed.isSuccess()) {
            SeismogramSource dataCenter = null;
            synchronized(dcLocator) {
                try {
                    dataCenter = dcLocator.getSeismogramSource(ecp.getEvent(),
                                                           ecp.getChannel(),
                                                           infilters,
                                                           ecp.getCookieJar());
                } catch(Throwable e) {
                    MotionVectorArm.handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e, dataCenter, requestToString(infilters, null));
                    return;
                }
            }
            RequestFilter[] outfilters = null;
            if(infilters.length > 0) {
                logger.debug("Trying available_data for " + ChannelIdUtil.toString(infilters[0].channel_id) + " from "
                        + infilters[0].start_time.date_time + " to " + infilters[0].end_time.date_time);
            } else {
                logger.debug("Empty request generated for " + ChannelIdUtil.toString(ecp.getChannel().get_id()));
            }
            if (Start.getRunProps().isSkipAvailableData()) {
                outfilters = infilters;
            } else {
                logger.debug("before available_data call retries=");
                MicroSecondDate before = new MicroSecondDate();
                outfilters = DataCenterSource.toArray(dataCenter.available_data(DataCenterSource.toList(infilters)));
                MicroSecondDate after = new MicroSecondDate();
                logger.info("After successful available_data call, time taken=" + after.subtract(before).getValue(UnitImpl.SECOND)+" sec");
                if(outfilters.length != 0) {
                    logger.debug("Got available_data for " + ChannelIdUtil.toString(outfilters[0].channel_id) + " from "
                                 + outfilters[0].start_time.date_time + " to " + outfilters[0].end_time.date_time);
                } else {
                    logger.debug("No available_data for " + ChannelIdUtil.toString(ecp.getChannel().get_id()));
                }
            }
            processAvailableDataSubsetter(ecp, dataCenter, infilters, SortTool.byBeginTimeAscending(outfilters));
        } else {
            ecp.update(Status.get(Stage.REQUEST_SUBSETTER, Standing.REJECT));
            failLogger.info(ecp.toString());
        }
    }

    public void processAvailableDataSubsetter(EventChannelPair ecp,
                                              SeismogramSource dataCenter,
                                              RequestFilter[] infilters,
                                              RequestFilter[] outfilters) {
        StringTree passed;
        synchronized(availData) {
            try {
                passed = availData.accept(ecp.getEvent(), ecp.getChannel(), infilters, outfilters, ecp.getCookieJar());
            } catch(Throwable e) {
                MotionVectorArm.handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e, dataCenter, requestToString(infilters, outfilters));
                return;
            }
        }
        if(passed.isSuccess()) {
            for(int i = 0; i < infilters.length; i++) {
                logger.debug("Getting seismograms " + ChannelIdUtil.toString(infilters[i].channel_id) + " from "
                        + infilters[i].start_time.date_time + " to " + infilters[i].end_time.date_time);
            } // end of for (int i=0; i<outFilters.length; i++)
            // Using infilters as asking for extra should not hurt
            MicroSecondDate before = new MicroSecondDate();
            LocalSeismogram[] localSeismograms = new LocalSeismogram[0];
            if(outfilters.length != 0) {
                
                
                
                
                try {
                    localSeismograms = DataCenterSource.toSeisArray(dataCenter.retrieveData(DataCenterSource.toList(infilters)));
                } catch(SeismogramSourceException e) {
                    MotionVectorArm.handle(ecp, Stage.DATA_RETRIEVAL, e, dataCenter, requestToString(infilters, outfilters));
                    return;
                } catch(org.omg.CORBA.SystemException e) {
                    MotionVectorArm.handle(ecp, Stage.DATA_RETRIEVAL, e, dataCenter, requestToString(infilters, outfilters));
                    return;
                }
                logger.debug("after successful retrieve_seismograms");
                if(localSeismograms.length > 0
                        && !ChannelIdUtil.areEqual(localSeismograms[0].channel_id, infilters[0].channel_id)) {
                    // must be server error
                    logger.warn("X Channel id in returned seismogram doesn not match channelid in request. req="
                            + ChannelIdUtil.toString(infilters[0].channel_id)
                            + " seis="
                            + ChannelIdUtil.toString(localSeismograms[0].channel_id));
                }
            } else {
                failLogger.info(ecp + " retrieve data returned no requestFilters: ");
                localSeismograms = new LocalSeismogram[0];
            } // end of else
            MicroSecondDate after = new MicroSecondDate();
            logger.info("After getting "+localSeismograms.length+" seismograms, time taken=" + after.subtract(before).getValue(UnitImpl.SECOND)+" sec");
            LinkedList tempForCast = new LinkedList();
            for(int i = 0; i < localSeismograms.length; i++) {
                if(localSeismograms[i] == null) {
                    ecp.update(Status.get(Stage.DATA_RETRIEVAL, Standing.REJECT));
                    logger.error("Got null in seismogram array " + ChannelIdUtil.toString(ecp.getChannel().get_id()));
                    return;
                }
                Channel ecpChan = ecp.getChannel();
                if(!ChannelIdUtil.areEqual(localSeismograms[i].channel_id, infilters[0].channel_id)) {
                    // must be server error
                    logger.warn("Channel id in returned seismogram doesn not match channelid in request. req="
                            + ChannelIdUtil.toStringFormatDates(infilters[0].channel_id) + " seis="
                            + ChannelIdUtil.toStringFormatDates(localSeismograms[i].channel_id));
                    // fix seis with original id
                    localSeismograms[i].channel_id = ecpChan.get_id();
                } // end of if ()
                tempForCast.add(localSeismograms[i]);
            } // end of for (int i=0; i<localSeismograms.length; i++)
            LocalSeismogramImpl[] tempLocalSeismograms = (LocalSeismogramImpl[])tempForCast.toArray(new LocalSeismogramImpl[0]);
            processSeismograms(ecp, dataCenter, infilters, outfilters, SortTool.byBeginTimeAscending(tempLocalSeismograms));
        } else {
            if(ClockUtil.now().subtract(Start.getRunProps().getSeismogramLatency()).after(ecp.getEvent()
                    .getOrigin()
                    .getTime())) {
                logger.info("Retry Reject, older than acceptible latency: "+Start.getRunProps().getSeismogramLatency()+" "+ecp);
                ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.REJECT));
            } else if (ecp.getNumRetries() >= SodDB.getSingleton().getMaxRetries()){
                logger.info("Retry Reject, at max retries: "+SodDB.getSingleton().getMaxRetries()+" "+ecp);
                ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.REJECT));
            } else {
                logger.info("Retry Retry, within acceptible latency: "+Start.getRunProps().getSeismogramLatency()+" "+ecp);
                ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.RETRY));
            }
            failLogger.info(ecp + ": " + passed.toString());
        }
    }

    public void processSeismograms(EventChannelPair ecp,
                                   SeismogramSource dataCenter,
                                   RequestFilter[] infilters,
                                   RequestFilter[] outfilters,
                                   LocalSeismogramImpl[] localSeismograms) {
        WaveformProcess processor = null;
        Iterator<WaveformProcess> it = processes.iterator();
        WaveformResult result = new WaveformResult(true, localSeismograms, this);
        try {
            while (it.hasNext() && result.isSuccess()) {
                processor = it.next();
                result = runProcessorThreadCheck(processor,
                                                 ecp.getEvent(),
                                                 ecp.getChannel(),
                                                 infilters,
                                                 outfilters,
                                                 result.getSeismograms(),
                                                 ecp.getCookieJar());
            } // end of while (it.hasNext())
            logger.debug("finished with " + ChannelIdUtil.toStringNoDates(ecp.getChannel().get_id()) + " success="
                    + result.isSuccess());
            if (result.isSuccess()) {
                ecp.update(Status.get(Stage.PROCESSOR, Standing.SUCCESS));
            } else {
                ecp.update(Status.get(Stage.PROCESSOR, Standing.REJECT));
                failLogger.info(ecp + " " + result.getReason());
            }
        } catch(Throwable e) {
            MotionVectorArm.handle(ecp, Stage.PROCESSOR, e, dataCenter, requestToString(infilters, outfilters));
            ecp.update(Status.get(Stage.PROCESSOR, Standing.SYSTEM_FAILURE));
            failLogger.info(ecp + " " + e);
        }
        logger.debug("finished with " + ChannelIdUtil.toStringNoDates(ecp.getChannel().get_id()));
    }

    public static WaveformResult runProcessorThreadCheck(WaveformProcess processor,
                                                               CacheEvent event,
                                                               ChannelImpl channel,
                                                               RequestFilter[] original,
                                                               RequestFilter[] available,
                                                               LocalSeismogramImpl[] seismograms,
                                                               CookieJar cookieJar) throws Exception {
        WaveformResult out;
        if (processor instanceof Threadable && ((Threadable)processor).isThreadSafe()) {
            out = internalRunProcessor(processor, event, channel, original, available, seismograms, cookieJar);
        } else {
            synchronized(processor) {
                out = internalRunProcessor(processor, event, channel, original, available, seismograms, cookieJar);
            }
        }
        if (out == null) {
            // badly behaved processor, assume success???
            logger.warn("Processor "+processor.getClass().getName()+" returned null for WaveformResult: "+processor.getClass());
            return new WaveformResult(seismograms, new Pass(processor));
        }
        return out;
    }
    
    private static WaveformResult internalRunProcessor(WaveformProcess processor,
                                                       CacheEvent event,
                                                       ChannelImpl channel,
                                                       RequestFilter[] original,
                                                       RequestFilter[] available,
                                                       LocalSeismogramImpl[] seismograms,
                                                       CookieJar cookieJar) throws Exception {
        WaveformResult result;
        try {
            result = processor.accept(event, channel, original, available, seismograms, cookieJar);
        } catch(FissuresException e) {
            if (e.getCause() instanceof CodecException) {
                result = new WaveformResult(seismograms, new Fail(processor, "Unable to decompress data", e));
            } else {
                throw e;
            }
        } catch(CodecException e) {
            result = new WaveformResult(seismograms, new Fail(processor, "Unable to decompress data", e));
        }
        return result;
    }

    protected static String requestToString(RequestFilter[] in, RequestFilter[] avail) {
        String message = "";
        message += "\n in=" + RequestFilterUtil.toString(in);
        message += "\n avail=" + RequestFilterUtil.toString(avail);
        return message;
    }
    

    private EventChannelSubsetter eventChannel = new PassEventChannel();

    private RequestGenerator requestGenerator;

    private RequestSubsetter request = new PassRequest();
    
    private AvailableDataSubsetter availData = defaultAvailableDataSubsetter;

    private LinkedList<WaveformProcess> processes = new LinkedList<WaveformProcess>();

    private static final Logger logger = LoggerFactory.getLogger(LocalSeismogramArm.class);

    private static final org.slf4j.Logger failLogger = org.slf4j.LoggerFactory.getLogger("Fail.Waveform");
}// LocalSeismogramArm
