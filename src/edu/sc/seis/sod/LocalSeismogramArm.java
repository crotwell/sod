package edu.sc.seis.sod;

import java.util.Iterator;
import java.util.LinkedList;
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
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.WaveformResult;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.availableData.PassAvailableData;
import edu.sc.seis.sod.subsetter.dataCenter.NullSeismogramDCLocator;
import edu.sc.seis.sod.subsetter.dataCenter.SeismogramDCLocator;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.PassEventChannel;
import edu.sc.seis.sod.subsetter.request.PassRequest;
import edu.sc.seis.sod.subsetter.request.Request;
import edu.sc.seis.sod.subsetter.requestGenerator.NullRequestGenerator;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;

public class LocalSeismogramArm implements Subsetter {
    public void handle(Object sodElement){
        if(sodElement instanceof EventChannelSubsetter) {
            eventChannel = (EventChannelSubsetter)sodElement;
        } else if(sodElement instanceof RequestGenerator) {
            requestGenerator = (RequestGenerator)sodElement;
        } else if(sodElement instanceof Request) {
            request = (Request)sodElement;
        } else if(sodElement instanceof SeismogramDCLocator) {
            dcLocator = (SeismogramDCLocator)sodElement;
        } else if(sodElement instanceof AvailableDataSubsetter) {
            availData = (AvailableDataSubsetter)sodElement;
        } else if(sodElement instanceof WaveformProcess) {
            processes.add(sodElement);
        } else {
            logger.warn("Unknown tag in LocalSeismogramArm config. "
                            + sodElement);
        } // end of else
    }

    public EventChannelSubsetter getEventChannelSubsetter() {
        return eventChannel;
    }

    public RequestGenerator getRequestGenerator() {
        return requestGenerator;
    }

    public Request getRequestSubsetter() {
        return request;
    }

    public AvailableDataSubsetter getAvailableDataSubsetter() {
        return availData;
    }

    public SeismogramDCLocator getSeismogramDCLocator() {
        return dcLocator;
    }

    public WaveformProcess[] getProcesses() {
        return (WaveformProcess[])processes.toArray(new WaveformProcess[0]);
    }

    public void processLocalSeismogramArm(EventChannelPair ecp) {
        boolean passed;
        EventAccessOperations eventAccess = ecp.getEvent();
        Channel channel = ecp.getChannel();
        synchronized(eventChannel) {
            try {
                passed = eventChannel.accept(eventAccess,
                                             channel,
                                             ecp.getCookieJar());
            } catch(Throwable e) {
                handle(ecp, Stage.EVENT_CHANNEL_SUBSETTER, e);
                return;
            }
        }
        if(passed) {
            ecp.update(Status.get(Stage.REQUEST_SUBSETTER, Standing.IN_PROG));
            processRequestGeneratorSubsetter(ecp);
        } else {
            ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                  Standing.REJECT));
            failLogger.info(ecp);
        }
    }

    public void processRequestGeneratorSubsetter(EventChannelPair ecp) {
        RequestFilter[] infilters;
        synchronized(requestGenerator) {
            try {
                infilters = requestGenerator.generateRequest(ecp.getEvent(),
                                                             ecp.getChannel(),
                                                             ecp.getCookieJar());
            } catch(Throwable e) {
                handle(ecp, Stage.REQUEST_SUBSETTER, e);
                return;
            }
        }
        processRequestSubsetter(ecp, DisplayUtils.sortByDate(infilters));
    }

    public void processRequestSubsetter(EventChannelPair ecp,
                                        RequestFilter[] infilters) {
        boolean passed;
        synchronized(request) {
            try {
                passed = request.accept(ecp.getEvent(),
                                        ecp.getChannel(),
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
                    dataCenter = dcLocator.getSeismogramDC(ecp.getEvent(),
                                                           ecp.getChannel(),
                                                           infilters,
                                                           ecp.getCookieJar());
                } catch(Throwable e) {
                    handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                    return;
                }
            }
            RequestFilter[] outfilters = null;
            logger.debug("Trying available_data for "
                             + ChannelIdUtil.toString(infilters[0].channel_id)
                             + " from " + infilters[0].start_time.date_time + " to "
                             + infilters[0].end_time.date_time);
            int retries = 0;
            int MAX_RETRY = 5;
            while(retries < MAX_RETRY) {
                try {
                    logger.debug("before available_data call retries="
                                     + retries);
                    outfilters = dataCenter.available_data(infilters);
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
                            Thread.sleep(sleepTime * 1000); // change seconds to
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
            if(outfilters.length != 0) {
                logger.debug("Got available_data for "
                                 + ChannelIdUtil.toString(outfilters[0].channel_id)
                                 + " from " + outfilters[0].start_time.date_time
                                 + " to " + outfilters[0].end_time.date_time);
            } else {
                logger.debug("No available_data for "
                                 + ChannelIdUtil.toString(infilters[0].channel_id));
            }
            processAvailableDataSubsetter(ecp,
                                          dataCenter,
                                          infilters,
                                          DisplayUtils.sortByDate(outfilters));
        } else {
            ecp.update(Status.get(Stage.REQUEST_SUBSETTER, Standing.RETRY));
            failLogger.info(ecp);
        }
    }

    public void processAvailableDataSubsetter(EventChannelPair ecp,
                                              ProxySeismogramDC dataCenter,
                                              RequestFilter[] infilters,
                                              RequestFilter[] outfilters) {
        boolean passed;
        synchronized(availData) {
            try {
                passed = availData.accept(ecp.getEvent(),
                                          ecp.getChannel(),
                                          infilters,
                                          outfilters,
                                          ecp.getCookieJar());
            } catch(Throwable e) {
                handle(ecp, Stage.AVAILABLE_DATA_SUBSETTER, e);
                return;
            }
        }
        if(passed) {
            ecp.update(Status.get(Stage.DATA_SUBSETTER, Standing.IN_PROG));
            for(int i = 0; i < infilters.length; i++) {
                logger.debug("Getting seismograms "
                                 + ChannelIdUtil.toString(infilters[i].channel_id)
                                 + " from " + infilters[i].start_time.date_time + " to "
                                 + infilters[i].end_time.date_time);
            } // end of for (int i=0; i<outFilters.length; i++)
            logger.debug("Using infilters, fix this when DMC fixes server");
            MicroSecondDate before = new MicroSecondDate();
            LocalSeismogram[] localSeismograms = new LocalSeismogram[0];
            if(outfilters.length != 0) {
                int retries = 0;
                int MAX_RETRY = 5;
                while(retries < MAX_RETRY) {
                    try {
                        logger.debug("before retrieve_seismograms");
                        NSSeismogramDC nsDC = (NSSeismogramDC)dataCenter.getWrappedDC(NSSeismogramDC.class);
                        if(nsDC.getServerDNS().equals("edu/iris/dmc")
                           && nsDC.getServerName()
                           .equals("IRIS_ArchiveDataCenter")) {
                            synchronized(this) {
                                //Archive doesn't support retrieve_seismograms
                                //so try using the queue set of retrieve calls
                                try {
                                    String id = dataCenter.queue_seismograms(infilters);
                                    logger.info("request id: " + id);
                                    String status = dataCenter.request_status(id);
                                    while(status.equals(RETRIEVING_DATA)) {
                                        try {
                                            Thread.sleep(60 * 1000);
                                        } catch(InterruptedException ex) {}
                                        status = dataCenter.request_status(id);
                                    }
                                    if(status.equals(DATA_RETRIEVED)) {
                                        localSeismograms = dataCenter.retrieve_queue(id);
                                    }
                                } catch(FissuresException ex) {
                                    handle(ecp, Stage.DATA_SUBSETTER, ex);
                                    return;
                                }
                            }
                        } else {
                            try {
                                localSeismograms = dataCenter.retrieve_seismograms(infilters);
                                for(int j = 0; j < localSeismograms.length; j++) {
                                    if(UnitImpl.createUnitImpl(localSeismograms[j].y_unit)
                                       .equals(COUNT_SQR)) {
                                        logger.debug("NOAMP get seis units="
                                                         + localSeismograms[j].y_unit);
                                        localSeismograms[j].y_unit = UnitImpl.COUNT;
                                    }
                                }
                            } catch(FissuresException e) {
                                handle(ecp, Stage.DATA_SUBSETTER, e);
                                return;
                            }
                        }
                        logger.debug("after successful retrieve_seismograms");
                        if(localSeismograms.length > 0
                           && !ChannelIdUtil.areEqual(localSeismograms[0].channel_id,
                                                      infilters[0].channel_id)) {
                            // must be server error
                            logger.warn("X Channel id in returned seismogram doesn not match channelid in request. req="
                                            + ChannelIdUtil.toString(infilters[0].channel_id)
                                            + " seis="
                                            + ChannelIdUtil.toString(localSeismograms[0].channel_id));
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
                failLogger.info(ecp+" retrieve data returned no requestFilters: ");
                localSeismograms = new LocalSeismogram[0];
            } // end of else
            MicroSecondDate after = new MicroSecondDate();
            logger.info("After getting seismograms, time taken="
                            + after.subtract(before));
            LinkedList tempForCast = new LinkedList();
            for(int i = 0; i < localSeismograms.length; i++) {
                if(localSeismograms[i] == null) {
                    ecp.update(Status.get(Stage.DATA_SUBSETTER, Standing.REJECT));
                    logger.error("Got null in seismogram array "
                                     + ChannelIdUtil.toString(ecp.getChannel().get_id()));
                    return;
                }
                Channel ecpChan = ecp.getChannel();
                if(!ChannelIdUtil.areEqual(localSeismograms[i].channel_id,
                                           ecpChan.get_id())) {
                    // must be server error
                    logger.warn("Channel id in returned seismogram doesn not match channelid in request. req="
                                    + ChannelIdUtil.toString(ecpChan.get_id())
                                    + " seis="
                                    + ChannelIdUtil.toString(localSeismograms[i].channel_id));
                    // fix seis with original id
                    localSeismograms[i].channel_id = ecpChan.get_id();
                } // end of if ()
                tempForCast.add(localSeismograms[i]);
            } // end of for (int i=0; i<localSeismograms.length; i++)
            LocalSeismogramImpl[] tempLocalSeismograms = (LocalSeismogramImpl[])tempForCast.toArray(new LocalSeismogramImpl[0]);
            processSeismograms(ecp, infilters, outfilters, DisplayUtils.sortByDate(tempLocalSeismograms));
        } else {
            ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                  Standing.RETRY));
            failLogger.info(ecp);
        }
    }

    public void processSeismograms(EventChannelPair ecp,
                                   RequestFilter[] infilters,
                                   RequestFilter[] outfilters,
                                   LocalSeismogramImpl[] localSeismograms) {
        WaveformProcess processor;
        Iterator it = processes.iterator();
        WaveformResult result = new WaveformResult(true,
                                                   localSeismograms);
        while(it.hasNext() && result.isSuccess()) {
            processor = (WaveformProcess)it.next();
            try {
                synchronized(processor) {
                    result = processor.process(ecp.getEvent(),
                                               ecp.getChannel(),
                                               infilters,
                                               outfilters,
                                               result.getSeismograms(),
                                               ecp.getCookieJar());
                }
            } catch(Throwable e) {
                handle(ecp, Stage.PROCESSOR, e);
                return;
            }
        } // end of while (it.hasNext())
        logger.debug("finished with "
                         + ChannelIdUtil.toStringNoDates(ecp.getChannel().get_id())
                         + " success=" + result.isSuccess());
        if(result.isSuccess()) {
            ecp.update(Status.get(Stage.PROCESSOR, Standing.SUCCESS));
        } else {
            ecp.update(Status.get(Stage.PROCESSOR, Standing.REJECT));
            failLogger.info(ecp+" "+result.getReason());
        }
    }

    private static void handle(EventChannelPair ecp, Stage stage, Throwable t) {
        if(t instanceof org.omg.CORBA.SystemException) {
            ecp.update(t, Status.get(stage, Standing.CORBA_FAILURE));
        } else {
            ecp.update(t, Status.get(stage, Standing.SYSTEM_FAILURE));
        }
        failLogger.warn(ecp, t);
    }

    private EventChannelSubsetter eventChannel = new PassEventChannel();

    private RequestGenerator requestGenerator = new NullRequestGenerator();

    private Request request = new PassRequest();

    private AvailableDataSubsetter availData = new PassAvailableData();

    private LinkedList processes = new LinkedList();

    private SeismogramDCLocator dcLocator = new NullSeismogramDCLocator();

    public static final String NO_DATA = "no_data";

    public static final String DATA_RETRIEVED = "Finished";

    public static final String RETRIEVING_DATA = "Processing";

    private static final UnitImpl COUNT_SQR = new UnitImpl(UnitBase.COUNT,
                                                           10,
                                                           "Dumb COUNT Squared",
                                                           1,
                                                           2);

    private static final Logger logger = Logger.getLogger(LocalSeismogramArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.Waveform");

}// LocalSeismogramArm
