/**
 * MotionVectorArm.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.UnitBase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.vector.ANDWaveformProcessWrapper;
import edu.sc.seis.sod.process.waveform.vector.WaveformProcessWrapper;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorProcess;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorProcessWrapper;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorResult;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.availableData.PassAvailableData;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.dataCenter.SeismogramDCLocator;
import edu.sc.seis.sod.subsetter.eventChannel.PassEventChannel;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;
import edu.sc.seis.sod.subsetter.request.PassRequest;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequest;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;
import edu.sc.seis.sod.subsetter.requestGenerator.vector.RequestGeneratorWrapper;
import edu.sc.seis.sod.subsetter.requestGenerator.vector.VectorRequestGenerator;

public class MotionVectorArm implements Subsetter {

    public WaveformVectorProcess[] getProcesses() {
        WaveformVectorProcess[] result = new WaveformVectorProcess[processes.size()];
        return (WaveformVectorProcess[])processes.toArray(result);
    }

    public WaveformProcess[] getWaveformProcesses() {
        List waveformProcesses = getWaveformProcesses(getProcesses());
        return (WaveformProcess[])waveformProcesses.toArray(new WaveformProcess[0]);
    }

    public List getWaveformProcesses(WaveformVectorProcess[] procs) {
        List waveformProcesses = new ArrayList();
        for(int i = 0; i < procs.length; i++) {
            if(procs[i] instanceof WaveformVectorProcessWrapper) {
                WaveformVectorProcess[] subProcesses = ((WaveformVectorProcessWrapper)procs[i]).getWrappedProcessors();
                waveformProcesses.addAll(getWaveformProcesses(subProcesses));
            } else if(procs[i] instanceof WaveformProcessWrapper) {
                WaveformProcessWrapper wrapper = (WaveformProcessWrapper)procs[i];
                waveformProcesses.add(wrapper.getWrappedProcess());
            }
        }
        return waveformProcesses;
    }

    public void handle(Object sodElement) {
        if(sodElement instanceof EventVectorSubsetter) {
            eventChannelGroup = (EventVectorSubsetter)sodElement;
        } else if(sodElement instanceof VectorRequestGenerator) {
            requestGenerator = (VectorRequestGenerator)sodElement;
        } else if(sodElement instanceof RequestGenerator) {
            requestGenerator = new RequestGeneratorWrapper((RequestGenerator)sodElement);
        } else if(sodElement instanceof VectorRequest) {
            request = (VectorRequest)sodElement;
        } else if(sodElement instanceof SeismogramDCLocator) {
            dcLocator = (SeismogramDCLocator)sodElement;
        } else if(sodElement instanceof VectorAvailableDataSubsetter) {
            availData = (VectorAvailableDataSubsetter)sodElement;
        } else if(sodElement instanceof WaveformVectorProcess) {
            processes.add(sodElement);
        } else if(sodElement instanceof WaveformProcess) {
            processes.add(new ANDWaveformProcessWrapper((WaveformProcess)sodElement));
        } else {
            logger.warn("Unknown tag in MotionVectorArm config. "
                    + sodElement.getClass().getName());
        } // end of else
    }

    public void processMotionVectorArm(EventVectorPair ecp) {
        StringTree passed;
        EventAccessOperations eventAccess = ecp.getEvent();
        ChannelGroup channel = ecp.getChannelGroup();
        synchronized(eventChannelGroup) {
            try {
                passed = eventChannelGroup.accept(eventAccess,
                                                  channel,
                                                  ecp.getCookieJar());
            } catch(Throwable e) {
                handle(ecp, Stage.EVENT_CHANNEL_SUBSETTER, e);
                return;
            }
        }
        if(passed.isSuccess()) {
            ecp.update(Status.get(Stage.REQUEST_SUBSETTER, Standing.IN_PROG));
            processRequestGeneratorSubsetter(ecp);
        } else {
            ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                  Standing.REJECT));
            failLogger.info(ecp);
        }
    }

    public void processRequestGeneratorSubsetter(EventVectorPair ecp) {
        RequestFilter[][] infilters;
        synchronized(requestGenerator) {
            try {
                infilters = requestGenerator.generateRequest(ecp.getEvent(),
                                                             ecp.getChannelGroup(),
                                                             ecp.getCookieJar());
                // check to see if at least one request filter exists, otherwise
                // fail
                boolean found = false;
                for(int i = 0; i < infilters.length; i++) {
                    if(infilters[i].length != 0) {
                        found = true;
                    }
                }
                if(!found) {
                    logger.info("FAIL no request generated");
                    ecp.update(Status.get(Stage.REQUEST_SUBSETTER,
                                          Standing.REJECT));
                    return;
                }
            } catch(Throwable e) {
                handle(ecp, Stage.REQUEST_SUBSETTER, e);
                return;
            }
        }
        processRequestSubsetter(ecp, infilters);
    }

    public void processRequestSubsetter(EventVectorPair ecp,
                                        RequestFilter[][] infilters) {
        boolean passed;
        synchronized(request) {
            try {
                passed = request.accept(ecp.getEvent(),
                                        ecp.getChannelGroup(),
                                        infilters,
                                        ecp.getCookieJar());
            } catch(Throwable e) {
                handle(ecp, Stage.REQUEST_SUBSETTER, e);
                return;
            }
        }
        if(passed) {
            ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                  Standing.IN_PROG));
            ProxySeismogramDC dataCenter;
            synchronized(dcLocator) {
                try {
                    //********************************************************
                    // WARNING, the dcLocator only uses the first channel!!! *
                    //********************************************************
                    dataCenter = dcLocator.getSeismogramDC(ecp.getEvent(),
                                                           ecp.getChannelGroup()
                                                                   .getChannels()[0],
                                                           infilters[0],
                                                           ecp.getCookieJar());
                } catch(Throwable e) {
                    handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                    return;
                }
            }
            RequestFilter[][] outfilters = new RequestFilter[ecp.getChannelGroup()
                    .getChannels().length][];
            for(int i = 0; i < outfilters.length; i++) {
                logger.debug("Trying available_data for "
                        + ChannelIdUtil.toString(infilters[0][0].channel_id)
                        + " from " + infilters[0][0].start_time.date_time
                        + " to " + infilters[0][0].end_time.date_time);
                int retries = 0;
                int MAX_RETRY = 5;
                while(retries < MAX_RETRY) {
                    try {
                        logger.debug("before available_data call retries="
                                + retries);
                        outfilters[i] = dataCenter.available_data(infilters[i]);
                        logger.debug("after successful available_data call retries="
                                + retries);
                        break;
                    } catch(org.omg.CORBA.SystemException e) {
                        retries++;
                        logger.debug("after failed available_data call retries="
                                + retries + " " + e.toString());
                        if(retries < MAX_RETRY) {
                            // sleep is 10 seconds times num retries
                            int sleepTime = 10 * retries;
                            logger.info("Caught CORBA exception, sleep for "
                                    + sleepTime + " then retry..." + retries, e);
                            try {
                                Thread.sleep(sleepTime * 1000); // change
                                // seconds to
                                // milliseconds
                            } catch(InterruptedException ex) {}
                            if(retries % 2 == 0) {
                                //force reload from name service evey other try
                                dataCenter.reset();
                            }
                        } else {
                            handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                            return;
                        }
                    }
                }
                if(outfilters[i].length != 0) {
                    logger.debug("Got available_data for "
                            + ChannelIdUtil.toString(outfilters[i][0].channel_id)
                            + " from " + outfilters[i][0].start_time.date_time
                            + " to " + outfilters[i][0].end_time.date_time);
                } else {
                    logger.debug("No available_data for "
                            + ChannelIdUtil.toString(infilters[i][0].channel_id));
                }
            }
            processAvailableDataSubsetter(ecp,
                                          dataCenter,
                                          infilters,
                                          outfilters);
        } else {
            ecp.update(Status.get(Stage.REQUEST_SUBSETTER, Standing.REJECT));
            failLogger.info(ecp);
        }
    }

    public void processAvailableDataSubsetter(EventVectorPair ecp,
                                              ProxySeismogramDC dataCenter,
                                              RequestFilter[][] infilters,
                                              RequestFilter[][] outfilters) {
        boolean passed;
        synchronized(availData) {
            try {
                passed = availData.accept(ecp.getEvent(),
                                          ecp.getChannelGroup(),
                                          infilters,
                                          outfilters,
                                          ecp.getCookieJar());
            } catch(Throwable e) {
                handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                return;
            }
        }
        if(passed) {
            ecp.update(Status.get(Stage.DATA_RETRIEVAL, Standing.IN_PROG));
            for(int i = 0; i < infilters.length; i++) {
                for(int j = 0; j < infilters[i].length; j++) {
                    logger.debug("Getting seismograms "
                            + ChannelIdUtil.toString(infilters[i][j].channel_id)
                            + " from " + infilters[i][j].start_time.date_time
                            + " to " + infilters[i][j].end_time.date_time);
                } // end of for (int i=0; i<outFilters.length; i++)
            }
            MicroSecondDate before = new MicroSecondDate();
            LocalSeismogram[][] localSeismograms = new LocalSeismogram[ecp.getChannelGroup()
                    .getChannels().length][0];
            LocalSeismogramImpl[][] tempLocalSeismograms = new LocalSeismogramImpl[ecp.getChannelGroup()
                    .getChannels().length][0];
            logger.debug("Using infilters, fix this when DMC fixes server");
            try {
                localSeismograms = getData(ecp, infilters, dataCenter);
                MicroSecondDate after = new MicroSecondDate();
                logger.info("After getting seismograms, time taken="
                        + after.subtract(before).convertTo(UnitImpl.SECOND));
                if(localSeismograms == null) { return; }
            } catch(FissuresException e) {
                handle(ecp, Stage.DATA_RETRIEVAL, e);
                return;
            }
            for(int i = 0; i < localSeismograms.length; i++) {
                LinkedList tempForCast = new LinkedList();
                for(int j = 0; j < localSeismograms[i].length; j++) {
                    if(UnitImpl.createUnitImpl(localSeismograms[i][j].y_unit)
                            .equals(COUNT_SQR)) {
                        logger.debug("NOAMP get seis units="
                                + localSeismograms[i][j].y_unit
                                + " changed to COUNT internally");
                        localSeismograms[i][j].y_unit = UnitImpl.COUNT;
                    }
                    if(localSeismograms[i][j] == null) {
                        ecp.update(Status.get(Stage.DATA_RETRIEVAL,
                                              Standing.REJECT));
                        logger.error("Got null in seismogram array for channel "
                                + i + " for " + ecp);
                        return;
                    }
                    Channel ecpChan = ecp.getChannelGroup().getChannels()[i];
                    if(!ChannelIdUtil.areEqual(localSeismograms[i][j].channel_id,
                                               ecpChan.get_id())) {
                        // must be server error
                        logger.warn("Channel id in returned seismogram doesn not match channelid in request. req="
                                + ChannelIdUtil.toString(ecpChan.get_id())
                                + " seis="
                                + ChannelIdUtil.toString(localSeismograms[i][j].channel_id));
                        // fix seis with original id
                        localSeismograms[i][j].channel_id = ecpChan.get_id();
                    } // end of if ()
                    tempForCast.add(localSeismograms[i][j]);
                } // end of for (int i=0; i<localSeismograms.length; i++)
                tempLocalSeismograms[i] = (LocalSeismogramImpl[])tempForCast.toArray(new LocalSeismogramImpl[0]);
            }
            processSeismograms(ecp, infilters, outfilters, tempLocalSeismograms);
        } else {
            ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                  Standing.REJECT));
            failLogger.info(ecp);
        }
    }

    public void processSeismograms(EventVectorPair ecp,
                                   RequestFilter[][] infilters,
                                   RequestFilter[][] outfilters,
                                   LocalSeismogramImpl[][] localSeismograms) {
        WaveformVectorProcess processor;
        WaveformVectorResult result = new WaveformVectorResult(true,
                                                               localSeismograms,
                                                               new StringTreeLeaf(this,
                                                                                  true));
        Iterator it = processes.iterator();
        while(it.hasNext() && result.isSuccess()) {
            processor = (WaveformVectorProcess)it.next();
            try {
                synchronized(processor) {
                    result = processor.process(ecp.getEvent(),
                                               ecp.getChannelGroup(),
                                               infilters,
                                               outfilters,
                                               result.getSeismograms(),
                                               ecp.getCookieJar());
                }
                if(!result.isSuccess()) {
                    logger.info("Processor reject: " + result.getReason());
                }
            } catch(Throwable e) {
                handle(ecp, Stage.PROCESSOR, e);
                return;
            }
        } // end of while (it.hasNext())
        logger.debug("finished with "
                + ChannelIdUtil.toStringNoDates(ecp.getChannelGroup()
                        .getChannels()[0].get_id()));
        if(result.isSuccess()) {
            ecp.update(Status.get(Stage.PROCESSOR, Standing.SUCCESS));
        } else {
            ecp.update(Status.get(Stage.PROCESSOR, Standing.REJECT));
            failLogger.info(ecp + " " + result.getReason());
        }
    }

    private LocalSeismogram[][] getData(EventVectorPair ecp,
                                        RequestFilter[][] rf,
                                        ProxySeismogramDC dataCenter)
            throws FissuresException {
        NSSeismogramDC nsDC = (NSSeismogramDC)dataCenter.getWrappedDC(NSSeismogramDC.class);
        // Special case for IRIS_ArchiveDataCenter
        // add all 3 sets of requests to archive, then go back and check
        if(nsDC.getServerDNS().equals("edu/iris/dmc")
                && nsDC.getServerName().equals("IRIS_ArchiveDataCenter")) {
            return getDataViaQueue(ecp, rf, dataCenter);
        } else {
            return getDataNormal(ecp, rf, dataCenter);
        }
    }

    private LocalSeismogram[][] getDataNormal(EventVectorPair ecp,
                                              RequestFilter[][] rf,
                                              ProxySeismogramDC dataCenter)
            throws FissuresException {
        LocalSeismogram[][] localSeismograms = new LocalSeismogram[rf.length][];
        for(int i = 0; i < rf.length; i++) {
            if(rf[i].length != 0) {
                int retries = 0;
                int MAX_RETRY = 5;
                while(retries < MAX_RETRY) {
                    try {
                        logger.debug("before retrieve_seismograms");
                        localSeismograms[i] = dataCenter.retrieve_seismograms(rf[i]);
                        logger.debug("after successful retrieve_seismograms");
                        if(localSeismograms[i].length > 0
                                && !ChannelIdUtil.areEqual(localSeismograms[i][0].channel_id,
                                                           rf[i][0].channel_id)) {
                            // must be server error
                            logger.warn("X Channel id in returned seismogram doesn not match channelid in request. req="
                                    + ChannelIdUtil.toString(rf[i][0].channel_id)
                                    + " seis="
                                    + ChannelIdUtil.toString(localSeismograms[i][0].channel_id));
                        }
                        break;
                    } catch(org.omg.CORBA.SystemException e) {
                        retries++;
                        logger.debug("after failed retrieve_seismograms, retries="
                                + retries);
                        if(retries < MAX_RETRY) {
                            logger.info("Caught CORBA exception, retrying..."
                                    + retries, e);
                            try {
                                Thread.sleep(1000 * retries);
                            } catch(InterruptedException ex) {}
                            if(retries % 2 == 0) {
                                // reget from Name service every other
                                // time
                                dataCenter.reset();
                            }
                        } else {
                            handle(ecp, Stage.DATA_RETRIEVAL, e);
                            return null;
                        }
                    }
                }
            } else {
                logger.debug("Failed, retrieve data returned no requestFilters ");
                localSeismograms[i] = new LocalSeismogram[0];
            } // end of else
        }
        return localSeismograms;
    }

    private LocalSeismogram[][] getDataViaQueue(EventVectorPair ecp,
                                                RequestFilter[][] rf,
                                                ProxySeismogramDC dataCenter)
            throws FissuresException {
        LocalSeismogram[][] localSeismograms = new LocalSeismogram[rf.length][];
        String[] id = new String[rf.length];
        // send each request to server
        for(int i = 0; i < id.length; i++) {
            try {
                id[i] = queueRequest(rf[i], dataCenter);
                logger.info("added to queue request id: " + id[i]);
            } catch(org.omg.CORBA.SystemException e) {
                handle(ecp, Stage.DATA_RETRIEVAL, e);
                return null;
            }
        }
        for(int i = 0; i < id.length; i++) {
            // keep checking until request is done, then retrieve it
            String status = LocalSeismogramArm.RETRIEVING_DATA;
            while(status.equals(LocalSeismogramArm.RETRIEVING_DATA)) {
                try {
                    status = statusRequest(id[i], dataCenter);
                } catch(org.omg.CORBA.SystemException e) {
                    handle(ecp, Stage.DATA_RETRIEVAL, e);
                    return null;
                }
                if(status.equals(LocalSeismogramArm.RETRIEVING_DATA)) {
                    try {
                        Thread.sleep(30 * 1000);
                    } catch(InterruptedException ex) {}
                }
            }
            if(status.equals(LocalSeismogramArm.DATA_RETRIEVED)) {
                try {
                    localSeismograms[i] = retrieveRequest(id[i], dataCenter);
                } catch(org.omg.CORBA.SystemException e) {
                    handle(ecp, Stage.DATA_RETRIEVAL, e);
                    return null;
                }
            } else {
                localSeismograms[i] = new LocalSeismogram[0];
                failLogger.info("Did not get seismogram " + i + " status was: "
                        + status);
            }
        }
        return localSeismograms;
    }

    private String queueRequest(RequestFilter[] rf, ProxySeismogramDC dataCenter)
            throws FissuresException {
        int retries = 0;
        int MAX_RETRY = 5;
        while(retries < MAX_RETRY) {
            try {
                return dataCenter.queue_seismograms(rf);
            } catch(org.omg.CORBA.SystemException e) {
                retries++;
                logger.debug("after failed queue_seismograms, retries="
                        + retries);
                if(retries < MAX_RETRY) {
                    logger.info("Caught CORBA exception, retrying..." + retries,
                                e);
                    try {
                        Thread.sleep(1000 * retries);
                    } catch(InterruptedException ex) {}
                    if(retries % 2 == 0) {
                        // reget from Name service every other
                        // time
                        dataCenter.reset();
                    }
                } else {
                    throw e;
                }
            }
        }
        // should never get here
        throw new RuntimeException("Should never happen");
    }

    private String statusRequest(String id, ProxySeismogramDC dataCenter)
            throws FissuresException {
        int retries = 0;
        int MAX_RETRY = 5;
        while(retries < MAX_RETRY) {
            try {
                return dataCenter.request_status(id);
            } catch(org.omg.CORBA.SystemException e) {
                retries++;
                logger.debug("after failed request_status, retries=" + retries);
                if(retries < MAX_RETRY) {
                    logger.info("Caught CORBA exception, retrying..." + retries,
                                e);
                    try {
                        Thread.sleep(1000 * retries);
                    } catch(InterruptedException ex) {}
                    if(retries % 2 == 0) {
                        // reget from Name service every other
                        // time
                        dataCenter.reset();
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Should never happen");
    }

    private LocalSeismogram[] retrieveRequest(String id,
                                              ProxySeismogramDC dataCenter)
            throws FissuresException {
        int retries = 0;
        int MAX_RETRY = 5;
        while(retries < MAX_RETRY) {
            try {
                return dataCenter.retrieve_queue(id);
            } catch(org.omg.CORBA.SystemException e) {
                retries++;
                logger.debug("after failed retrieve_queue, retries=" + retries);
                if(retries < MAX_RETRY) {
                    logger.info("Caught CORBA exception, retrying..." + retries,
                                e);
                    try {
                        Thread.sleep(1000 * retries);
                    } catch(InterruptedException ex) {}
                    if(retries % 2 == 0) {
                        // reget from Name service every other
                        // time
                        dataCenter.reset();
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Should never happen");
    }

    private static void handle(EventVectorPair ecp, Stage stage, Throwable t) {
        if(t instanceof org.omg.CORBA.SystemException) {
            ecp.update(t, Status.get(stage, Standing.CORBA_FAILURE));
        } else {
            ecp.update(t, Status.get(stage, Standing.SYSTEM_FAILURE));
        }
        failLogger.warn(ecp, t);
    }

    private EventVectorSubsetter eventChannelGroup = new PassEventChannel();

    private VectorRequestGenerator requestGenerator;

    private VectorRequest request = new PassRequest();

    private SeismogramDCLocator dcLocator;

    private VectorAvailableDataSubsetter availData = new PassAvailableData();

    private LinkedList processes = new LinkedList();

    private static final Logger logger = Logger.getLogger(MotionVectorArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.WaveformVector");

    private static final UnitImpl COUNT_SQR = new UnitImpl(UnitBase.COUNT,
                                                           10,
                                                           "Dumb COUNT Squared",
                                                           1,
                                                           2);
}