package edu.sc.seis.sod;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.GenericJDBCException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.vector.ANDWaveformProcessWrapper;
import edu.sc.seis.sod.status.OutputScheduler;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import edu.sc.seis.sod.subsetter.EventEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.eventStation.PassEventStation;

public class WaveformArm implements Arm {

    public WaveformArm(Element config,
                       EventArm eventArm,
                       NetworkArm networkArm,
                       int threadPoolSize) throws Exception {
        initDb();
        processConfig(config);
        this.networkArm = networkArm;
        this.eventArm = eventArm;
        pool = new WorkerThreadPool("Waveform EventChannel Processor",
                                    threadPoolSize);
    }
    
    private void initDb() throws SQLException {
        RunProperties runProps = Start.getRunProps();
        SERVER_RETRY_DELAY = runProps.getServerRetryDelay();
        sodDb = new SodDB();
        logger.info("SodDB in WaveformArm:"+sodDb);
        eventDb = new StatefulEventDB();
    }

    public boolean isActive() {
        return !finished;
    }

    public String getName() {
        return "WaveformArm";
    }

    public void run() {
        try {
            addSuspendedPairsToQueue(Start.suspendedPairs);
            while(pool.isEmployed()) {
                try {
                    logger.debug("pool employed, sleeping for 100 sec");
                    Thread.sleep(100000);
                } catch(InterruptedException e) {}
            }
            waitForInitialEvent();
            int sleepTime = 5;
            TimeInterval logInterval = new TimeInterval(10, UnitImpl.MINUTE);
            logger.debug("will populateEventChannelDb, then sleep "
                    + sleepTime
                    + " sec between each try to process successful events, log interval is "
                    + logInterval);
            lastEventStartLogTime = ClockUtil.now();
            do {
                int numEvents = populateEventChannelDb();
                retryIfNeededAndAvailable();
                sleepALittle(numEvents, sleepTime, logInterval);
            } while(possibleToContinue());
            logger.info("Main waveform arm done.  Retrying failures.");
            List allRetries = sodDb.getAllRetries();
            Iterator it = allRetries.iterator();
            while (it.hasNext()) {
                while(pool.getNumWaiting() > poolLineCapacity) {
                    try {
                    Thread.sleep(100);
                    } catch(InterruptedException e) {}
                }
                WaveformWorkUnit retryUnit = (WaveformWorkUnit)it.next();
                pool.invokeLater(retryUnit);
            }
            while(!Start.isArmFailure() && pool.isEmployed()) {
                try {
                    logger.debug("Sleeping while waiting for retries");
                    Thread.sleep(10000);
                } catch(InterruptedException e) {}
            }
            logger.info("Lo!  I am weary of my wisdom, like the bee that hath gathered too much\n"
                    + "honey; I need hands outstretched to take it.");
        } catch(Throwable e) {
            Start.armFailure(this, e);
        }
        finished = true;
        synchronized(OutputScheduler.getDefault()) {
            OutputScheduler.getDefault().notify();
        }
    }

    private boolean possibleToContinue() {
        return !Start.isArmFailure() && eventArm.isActive();
    }

    private void sleepALittle(int numEvents,
                              int sleepTime,
                              TimeInterval logInterval) {
        if(numEvents != 0) {
            // found events so reset logInterval
            lastEventStartLogTime = ClockUtil.now();
        } else if(ClockUtil.now()
                .subtract(lastEventStartLogTime)
                .greaterThan(logInterval)) {
            logger.debug("no successful events found in last " + logInterval);
            lastEventStartLogTime = ClockUtil.now();
        }
        try {
            Thread.sleep(sleepTime * 1000);
        } catch(InterruptedException e) {}
    }

    public LocalSeismogramArm getLocalSeismogramArm() {
        return localSeismogramArm;
    }

    public MotionVectorArm getMotionVectorArm() {
        return motionVectorArm;
    }

    public EventStationSubsetter getEventStationSubsetter() {
        return eventStationSubsetter;
    }

    public WaveformMonitor[] getWaveformArmMonitors() {
        return (WaveformMonitor[])statusMonitors.toArray(new WaveformMonitor[0]);
    }

    // fills the eventchannel db with all available events and starts
    // WaveformWorkerUnits on all inserted event channel pairs
    // If there are no waiting events, this just returns
    private int populateEventChannelDb() throws Exception {
        int numEvents = 0;
        for(StatefulEvent ev = eventDb.getNext(); ev != null; ev = eventDb.getNext()) {
            logger.debug("Work on event dbid="+ev.getDbid());
            ev.setStatus(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                    Standing.IN_PROG));
            eventDb.commit();
            numEvents++;
            if (ev.get_preferred_origin().origin_time == null) {throw new RuntimeException("otime is null "+ev.get_preferred_origin().my_location);}
            EventEffectiveTimeOverlap overlap = new EventEffectiveTimeOverlap(ev);
            CacheNetworkAccess[] networks = networkArm.getSuccessfulNetworks();
            
            for(int i = 0; i < networks.length; i++) {
                startNetwork(ev, overlap, networks[i]);
            }
            // set the status of the event to be SUCCESS implying that
            // that all the network information for this particular event is
            // inserted
            // in the waveformDatabase.
            // reattach ev so we can set status
            eventDb.getSession().lock(ev, LockMode.NONE);
            ev.setStatus(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                    Standing.SUCCESS));
            eventArm.change(ev);
            int numWaiting = eventDb.getNumWaiting();
            if(numWaiting < EventArm.MIN_WAIT_EVENTS) {
                logger.debug("There are less than "
                        + EventArm.MIN_WAIT_EVENTS
                        + " waiting events.  Telling the eventArm to start up again");
                synchronized(Start.getEventArm()) {
                    Start.getEventArm().notify();
                }
            }
        }
        eventDb.commit();
        return numEvents;
    }

    private void startNetwork(StatefulEvent ev,
                              EventEffectiveTimeOverlap overlap,
                              CacheNetworkAccess net) throws Exception {
        // don't bother with network if effective time does no
        // overlap event time
        if(!overlap.overlaps(net.get_attributes())) {
            failLogger.info(NetworkIdUtil.toString(net
                    .get_attributes()
                    .get_id())
                    + "  The networks effective time does not overlap the event time.");
            return;
        } // end of if ()
        StationImpl[] stations = networkArm.getSuccessfulStations(net);
        for(int i = 0; i < stations.length; i++) {
            startStation(overlap, net, stations[i], ev);
        }
    }

    private void startStation(EventEffectiveTimeOverlap overlap,
                              CacheNetworkAccess net,
                              StationImpl station,
                              StatefulEvent ev) throws Exception {
        if(!overlap.overlaps(station)) {
            failLogger.debug(StationIdUtil.toString(station
                    .get_id())
                    + "  The stations effective time does not overlap the event time.");
            return;
        } // end of if ()
        ChannelImpl[] chans = networkArm.getSuccessfulChannels(net, station);
        if(motionVectorArm != null) {
            startChannelGroups(overlap, ev, chans);
        } else {
            // individual local seismograms
            for(int i = 0; i < chans.length; i++) {
                startChannel(overlap, chans[i], ev);
            }
        }
    }

    private void startChannelGroups(EventEffectiveTimeOverlap overlap,
                                    StatefulEvent ev,
                                    ChannelImpl[] chans)
            throws SQLException {
        List overlapList = new ArrayList();
        for(int i = 0; i < chans.length; i++) {
            if(overlap.overlaps(chans[i])) {
                overlapList.add(chans[i]);
            } else {
                logger.debug("The channel effective time does not overlap the event time");
            }
        }
        ChannelImpl[] overlapChans = (ChannelImpl[])overlapList.toArray(new ChannelImpl[0]);
        ChannelGroup[] chanGroups = groupChannels(overlapChans, ev);
        Status eventStationInit = Status.get(Stage.EVENT_STATION_SUBSETTER,
                                             Standing.INIT);
        for(int i = 0; i < chanGroups.length; i++) {
            int[] pairIds = new int[3];
            EventChannelPair[] ecpTmp = new EventChannelPair[3];
            for(int j = 0; j < pairIds.length; j++) {
                for(int k = 0; k < overlapChans.length; k++) {
                    if(ChannelIdUtil.areEqual(overlapChans[k]
                            .get_id(), chanGroups[i].getChannels()[j].get_id())) {
                        ecpTmp[j] = new EventChannelPair(ev, overlapChans[k], 0, eventStationInit);
                        break;
                    }
                }
            }
            EventVectorPair evp = new EventVectorPair(ecpTmp);
            sodDb.getSession().lock(ev, LockMode.NONE);
            sodDb.put(evp);
            sodDb.commit();
            invokeLaterAsCapacityAllows(new MotionVectorWaveformWorkUnit(evp));
            retryIfNeededAndAvailable();
        }
    }

    public ChannelGroup[] groupChannels(ChannelImpl[] chans, StatefulEvent ev)
            throws SQLException {
        Channel[] channels = new Channel[chans.length];
        for(int i = 0; i < channels.length; i++) {
            channels[i] = chans[i];
        }
        LinkedList failures = new LinkedList();
        ChannelGroup[] chanGroups = channelGrouper.group(channels, failures);
        Iterator it = failures.iterator();
        Status eventStationReject = Status.get(Stage.EVENT_STATION_SUBSETTER,
                                               Standing.REJECT);
        while(it.hasNext()) {
            Channel failchan = (Channel)it.next();
            failLogger.info(ChannelIdUtil.toString(failchan.get_id())
                    + "  Channel not grouped.");
            for(int k = 0; k < chans.length; k++) {
                if(ChannelIdUtil.areEqual(chans[k].get_id(),
                                          failchan.get_id())) {
                    int chanDbId = chans[k].getDbid();
                    sodDb.put(new EventChannelPair(ev, chans[k], 0, eventStationReject));
                }
            }
        }
        return chanGroups;
    }

    private void startChannel(EventEffectiveTimeOverlap overlap,
                              ChannelImpl chan,
                              StatefulEvent ev) throws Exception {
        if(!overlap.overlaps(chan)) {
            logger.debug("The channel effective time does not overlap the event time");
            return;
        } // end of if ()
        // attach channel as it came from network arm thread, and hence separate session
        chan = (ChannelImpl)sodDb.getSession().load(ChannelImpl.class, new Integer(chan.getDbid()));
        StationImpl sta = chan.getSite().getStation();
        ev = (StatefulEvent)sodDb.getSession().load(StatefulEvent.class, new Integer(ev.getDbid()));
        // cache the channelInformation.
        EventChannelPair pair = sodDb.put(new EventChannelPair(ev,
                                                               chan,
                                                               0,
                                                               Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                                          Standing.INIT)));
        try {
        sodDb.commit();
        }catch(GenericJDBCException e) {
            logger.error(e);
            if (e.getCause() instanceof BatchUpdateException) {
                BatchUpdateException b = (BatchUpdateException)e.getCause();
                logger.error(b);
                SQLException s = b.getNextException();
                logger.error(s);
            }
            throw e;
        }
        if(pair != null) {
            invokeLaterAsCapacityAllows(new LocalSeismogramWaveformWorkUnit(pair));
        }
        retryIfNeededAndAvailable();
    }

    private void retryIfNeededAndAvailable() throws SQLException {
        int numInPool = pool.getNumWaiting();
        if(numInPool == 0
                || getNumRetryWaiting() / (double)numInPool < retryPercentage) {
            retryIfAvailable();
        }
    }

    private void retryIfAvailable() throws SQLException {
        WaveformWorkUnit[] retryUnit = sodDb.getRetryWaveformWorkUnits(10);
        if(retryUnit != null) {
            for (int i = 0; i < retryUnit.length; i++) {
                logger.debug("retrying: "+retryUnit.toString());
                invokeLaterAsCapacityAllows(retryUnit[i]);
			}
        }
    }

    private void addSuspendedPairsToQueue(EventChannelPair[] pairs) throws SQLException {
        for(int i = 0; i < pairs.length; i++) {
            logger.debug("Starting suspended pair " + pairs[i]);
            WaveformWorkUnit workUnit = null;
            if(localSeismogramArm != null) {
                workUnit = new LocalSeismogramWaveformWorkUnit(pairs[i]);
            } else {
                    EventChannelPair ecp = pairs[i];
                    EventVectorPair ecgp = sodDb.getEventVectorPair(ecp);
                    if(ecgp != null && !usedPairGroups.contains(ecgp)) {
                        usedPairGroups.add(ecgp);
                        workUnit = new MotionVectorWaveformWorkUnit(ecgp);
                    }
            }
            if(workUnit != null) {
                logger.debug("Adding " + workUnit + " to pool");
                pool.invokeLater(workUnit);
            } else {
                logger.debug("Unable to find work unit for pair "+pairs[i].getPairId());
            }
        }
    }

    private int getNumRetryWaiting() {
        return retryNum;
    }

    /**
     * This method blocks until there is space in the pool for wu to run, then
     * starts its execution.
     */
    private void invokeLaterAsCapacityAllows(WaveformWorkUnit wu) {
        while(pool.getNumWaiting() > poolLineCapacity) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {}
        }
        pool.invokeLater(wu);
    }

    private StatefulEvent waitForInitialEvent() throws SQLException {
        StatefulEvent next;
        next = eventDb.getNext();
        
        while(possibleToContinue() && next == null) {
            logger.debug("Waiting for the first exciting event to show up");
            try {
                synchronized(this) {
                    wait();
                }
            } catch(InterruptedException e) {}
            next = eventDb.getNext();
        }
        return next;
    }

    public void add(WaveformProcess proc) {
        if(motionVectorArm != null) {
            motionVectorArm.add(new ANDWaveformProcessWrapper(proc));
        } else {
            localSeismogramArm.add(proc);
        }
    }

    protected void processConfig(Element config) throws ConfigurationException {
        if(config.getTagName().equals("waveformVectorArm")) {
            motionVectorArm = new MotionVectorArm();
            channelGrouper = new ChannelGrouper();
        } else {
            localSeismogramArm = new LocalSeismogramArm();
        }
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element el = (Element)children.item(i);
                Object sodElement = SodUtil.load(el, PACKAGES);
                if(sodElement instanceof EventStationSubsetter) {
                    eventStationSubsetter = (EventStationSubsetter)sodElement;
                } else if(sodElement instanceof WaveformMonitor) {
                    addStatusMonitor((WaveformMonitor)sodElement);
                } else {
                    if(localSeismogramArm != null) {
                        localSeismogramArm.handle(sodElement);
                    } else {
                        motionVectorArm.handle(sodElement);
                    }
                }
            } // end of if (node instanceof Element)
        } // end of for (intadd i=0; i<children.getSize(); i++)
    }

    public static final String[] PACKAGES = new String[] {"waveformArm",
                                                          "availableData",
                                                          "availableData.vector",
                                                          "eventChannel",
                                                          "eventChannel.vector",
                                                          "eventStation",
                                                          "request",
                                                          "request.vector",
                                                          "requestGenerator",
                                                          "requestGenerator.vector",
                                                          "waveform",
                                                          "waveform.vector",
                                                          "dataCenter"};

    public void addStatusMonitor(WaveformMonitor monitor) {
        statusMonitors.add(monitor);
    }

    public synchronized void setStatus(EventChannelPair ecp) {
        synchronized(statusMonitors) {
            Iterator it = statusMonitors.iterator();
            while(it.hasNext()) {
                try {
                    ((WaveformMonitor)it.next()).update(ecp);
                } catch(Exception e) {
                    // oh well, log it and go to next status processor
                    GlobalExceptionHandler.handle("Problem in setStatus", e);
                }
            }
        }
    }

    private MicroSecondDate lastEventStartLogTime;
    

    public static final String BIG_ERROR_MSG = "An exception occured that would've croaked a waveform worker thread!  These types of exceptions are certainly possible, but they shouldn't be allowed to percolate this far up the stack.  If you are one of those esteemed few working on SOD, it behooves you to attempt to trudge down the stack trace following this message and make certain that whatever threw this exception is no longer allowed to throw beyond its scope.  If on the other hand, you are a user of SOD it would be most appreciated if you would send an email containing the text immediately following this mesage to sod@seis.sc.edu";

    private boolean finished = false;

    private WorkerThreadPool pool;

    private EventStationSubsetter eventStationSubsetter = new PassEventStation();

    private LocalSeismogramArm localSeismogramArm = null;

    private MotionVectorArm motionVectorArm = null;

    private NetworkArm networkArm = null;

    private EventArm eventArm = null;

    private SodDB sodDb;
    
    private StatefulEventDB eventDb;

    private ChannelGrouper channelGrouper;

    private List usedPairGroups = new ArrayList();

    private double retryPercentage = .02;// 2 percent of the pool will be

    // Amount of time after the run has ended that we retry Server based
    // failures
    private TimeInterval SERVER_RETRY_DELAY;

    private static Logger logger = Logger.getLogger(WaveformArm.class);

    private static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.WaveformArm");

    private Set statusMonitors = Collections.synchronizedSet(new HashSet());

    private int poolLineCapacity = 100;
    
    int retryNum;

}
