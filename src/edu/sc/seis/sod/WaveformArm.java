package edu.sc.seis.sod;
import java.util.*;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.SiteDbObject;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelRetry;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import edu.sc.seis.sod.subsetter.waveformArm.EventEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.waveformArm.NullEventStationSubsetter;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WaveformArm implements Runnable {
    public WaveformArm(Element config, NetworkArm networkArm)
        throws Exception {
        this(config, networkArm, 5);
    }

    public WaveformArm(Element config, NetworkArm networkArm, int threadPoolSize)
        throws Exception {
        eventStatus = new JDBCEventStatus();
        evChanStatus = new JDBCEventChannelStatus();
        eventRetryTable = new JDBCEventChannelRetry();
        processConfig(config);
        this.networkArm = networkArm;
        pool = new WorkerThreadPool("Waveform EventChannel Processor", threadPoolSize);
        MAX_RETRY_DELAY =  Start.getRunProps().getMaxRetryDelay();
        SERVER_RETRY_DELAY = Start.getRunProps().getServerRetryDelay();
    }

    public boolean isFinished() { return finished;}

    public void run() {
        try {
            addSuspendedPairsToQueue(Start.suspendedPairs);
            while(pool.isEmployed()){
                try {
                    //System.out.println("isEmployed: " + pool.isEmployed());
                    //System.out.println("numWaiting: " + pool.getNumWaiting());
                    //System.out.println("numIdle: " + pool.getNumIdle());
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
            }
            waitForInitialEvent();
            do{
                populateEventChannelDb();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
                retryIfNeededAndAvailable();
            } while(Start.getEventArm().isAlive());
            logger.info("Main waveform arm done.  Retrying failures.");
            MicroSecondDate runFinishTime = ClockUtil.now();
            MicroSecondDate serverFailDelayEnd = runFinishTime.add(SERVER_RETRY_DELAY);
            while((serverFailDelayEnd.after(ClockUtil.now()) &&
                       serverFailuresRemain()) || availableDataFailuresRemain()){
                retryIfNeededAndAvailable();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
            }
            while(pool.isEmployed()){
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
            }
        } catch(Throwable e) {
            GlobalExceptionHandler.handle("Problem running waveform arm", e);
        }
        finished = true;
    }

    private boolean availableDataFailuresRemain() throws SQLException {
        synchronized(eventRetryTable){
            return eventRetryTable.availableDataRetriesRemain();
        }
    }

    private boolean serverFailuresRemain() throws SQLException {
        synchronized(eventRetryTable){
            return eventRetryTable.serverFailureRetriesRemain();
        }
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

    public WaveformArmMonitor[] getWaveformArmMonitors() {
        return (WaveformArmMonitor[])statusMonitors.toArray(new WaveformArmMonitor[0]);
    }

    //fills the eventchannel db with all available events and starts
    //WaveformWorkerUnits on all inserted event channel pairs
    //If there are no waiting events, this just returns
    private void populateEventChannelDb()throws Exception {
        for(EventDbObject ev = popAndGet(); ev != null; ev = popAndGet()){
            EventEffectiveTimeOverlap overlap =
                new EventEffectiveTimeOverlap(ev.getEvent());

            NetworkDbObject[] networks = networkArm.getSuccessfulNetworks();
            logger.debug("got " + networks.length + " networks from getSuccessfulNetworks()");
            for(int i = 0; i < networks.length; i++) {
                startNetwork(ev, overlap, networks[i]);
            }
            //set the status of the event to be SUCCESS implying that
            //that all the network information for this particular event is inserted
            //in the waveformDatabase.
            Start.getEventArm().change(ev.getEvent(),Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                Standing.SUCCESS));
        }
    }

    private void startNetwork(EventDbObject ev, EventEffectiveTimeOverlap overlap,
                              NetworkDbObject net)throws Exception{
        // don't bother with network if effective time does no
        // overlap event time
        if ( ! overlap.overlaps(net.getNetworkAccess().get_attributes())) {
            logger.debug("The networks effective time does not overlap the event time");
            return;
        } // end of if ()
        StationDbObject[] stations = networkArm.getSuccessfulStations(net);
        logger.debug("got " + stations.length + " SuccessfulStations");
        for(int i = 0; i < stations.length; i++) {
            startStation(overlap, net, stations[i], ev);
        }
    }

    private void startStation(EventEffectiveTimeOverlap overlap,
                              NetworkDbObject net,
                              StationDbObject station, EventDbObject ev) throws Exception{
        if ( ! overlap.overlaps(station.getStation())) {
            logger.debug("The stations effective time does not overlap the event time");

            return;
        } // end of if ()
        SiteDbObject[] sites = networkArm.getSuccessfulSites(net, station);
        logger.debug("got " + sites.length + " SuccessfulSites");
        for(int i = 0; i < sites.length; i++) {
            startSite(overlap, net, sites[i], ev);
        }
    }

    private void startSite(EventEffectiveTimeOverlap overlap, NetworkDbObject net,
                           SiteDbObject site, EventDbObject ev) throws Exception{
        if ( !overlap.overlaps(site.getSite())) {
            logger.debug("The sites effective time does not overlap the event time");
            return;
        } // end of if ()
        ChannelDbObject[] chans = networkArm.getSuccessfulChannels(net, site);
        logger.debug(ExceptionReporterUtils.getMemoryUsage()+" got " + chans.length + " SuccessfulChannels");
        if (motionVectorArm != null) {
            Channel[] channels = new Channel[chans.length];
            for (int i = 0; i < channels.length; i++) {
                channels[i] = chans[i].getChannel();
            }
            LinkedList failures = new LinkedList();
            ChannelGroup[] chanGroups = channelGrouper.group(channels, failures);
            Iterator it = failures.iterator();
            while(it.hasNext()) {
                Channel failchan = (Channel)it.next();
                for (int k = 0; k < chans.length; k++) {
                    if (ChannelIdUtil.areEqual(chans[k].getChannel().get_id(),
                                               failchan.get_id())) {
                        int pairId = evChanStatus.put(ev.getDbId(),
                                                      chans[k].getDbId(),
                                                      Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                                 Standing.INIT));
                        pairId = evChanStatus.put(ev.getDbId(),
                                                  chans[k].getDbId(),
                                                  Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                             Standing.REJECT));
                    }
                }
            }
            for (int i = 0; i < chanGroups.length; i++) {
                int[] pairIds = new int[3];
                for (int j = 0; j < pairIds.length; j++) {
                    for (int k = 0; k < chans.length; k++) {
                        if (ChannelIdUtil.areEqual(chans[k].getChannel().get_id(), chanGroups[i].getChannels()[j].get_id())) {
                            synchronized(evChanStatus){
                                pairIds[j] = evChanStatus.put(ev.getDbId(),
                                                              chans[k].getDbId(),
                                                              Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                                         Standing.INIT));

                            }
                            break;
                        }
                    }
                }
                invokeLaterAsCapacityAllows(new MotionVectorWaveformWorkUnit(pairIds));
                // retryIfNeededAndAvailable();
            }
        } else {
            // individual local seismograms
            for(int i = 0; i < chans.length; i++) {
                startChannel(overlap, chans[i], ev);
            }
        }
    }

    private void startChannel(EventEffectiveTimeOverlap overlap, ChannelDbObject chan, EventDbObject ev)throws Exception{
        if ( !overlap.overlaps(chan.getChannel())) {
            logger.debug("The channel effective time does not overlap the event time");
            return;
        } // end of if ()
        //cache the channelInformation.
        int pairId = -1;
        synchronized(evChanStatus){
            try{
                //getPairId to see if it exists
                evChanStatus.getPairId(ev.getDbId(), chan.getDbId());
            }
            catch (NotFound e){//pairId doesn't exist.  Putting it in the database.
                pairId = evChanStatus.put(ev.getDbId(), chan.getDbId(),
                                          Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                     Standing.INIT));
            }
        }
        if (pairId != -1){
            invokeLaterAsCapacityAllows(new LocalSeismogramWaveformWorkUnit(pairId));
        }
        retryIfNeededAndAvailable();
    }

    private void retryIfNeededAndAvailable() throws SQLException {
        if(getNumRetryWaiting()/(double)pool.getNumWaiting() < retryPercentage){
            retryIfAvailable();
        }
    }

    private void retryIfAvailable() throws SQLException{
        WaveformWorkUnit retryUnit = getNextRetry();
        if(retryUnit != null){ invokeLaterAsCapacityAllows(retryUnit); }
    }

    public EventChannelGroupPair getEventChannelGroupPair(EventChannelPair ecp) {
        try {
            Channel[] chans = evChanStatus.getAllChansForSite(ecp.getPairId());
            ChannelGroup[] groups = channelGrouper.group(chans, new ArrayList());
            ChannelGroup pairGroup = null;
            for (int i = 0; i < groups.length; i++) {
                if(groups[i].contains(ecp.getChannel())){
                    pairGroup = groups[i];
                    break;
                }
            }
            if(pairGroup == null){
                return null;
            }
            int[] pairIds = evChanStatus.getPairs(ecp.getEvent(), pairGroup);
            EventChannelPair[] pairs = new EventChannelPair[pairIds.length];
            synchronized(evChanStatus){
                for (int i = 0; i < pairIds.length; i++) {
                    pairs[i] = evChanStatus.get(pairIds[i], this);
                }
            }
            return new EventChannelGroupPair(pairs);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        } catch (NotFound e) {
            GlobalExceptionHandler.handle(e);
        }
        return null;
    }

    private WaveformWorkUnit getNextRetry() throws SQLException {
        int pairId;
        synchronized (eventRetryTable) {
            pairId = eventRetryTable.next();
        }
        if(pairId != -1) {
            if (motionVectorArm != null) {
                try {
                    EventChannelPair ecp;
                    try {
                        ecp = evChanStatus.get(pairId, this);
                    } catch (NotFound e) {
                        GlobalExceptionHandler.handle("EventChannelStatus table unable to find pair " + pairId + " right after it gave it to me",
                                                      e);
                        return null;
                    }
                    try {
                        EventChannelGroupPair ecgp = getEventChannelGroupPair(ecp);
                        if (ecgp == null){
                            return null;
                        }
                        return new RetryMotionVectorWaveformWorkUnit(evChanStatus.getPairs(ecgp));
                    } catch (NotFound e) {
                        GlobalExceptionHandler.handle("EventChannelStatus table unable to find pair right after it gave it to me",
                                                      e);
                    }
                } catch (SQLException e) {
                    GlobalExceptionHandler.handle("Trouble matching up a pair with its waveform group",
                                                  e);
                }
            } else {
                return new RetryWaveformWorkUnit(pairId);
            }
        }
        return null;
    }

    private void addSuspendedPairsToQueue(int[] pairIds) throws SQLException{

        //if we're going to run suspended pairs we need to prepopulate the networkArm.
        //That's what this lovely monsterous triple for-loop does.  You can remove it
        //once the network db returns real channels.
        if (pairIds.length > 0){
            try {
                NetworkDbObject[] nets= networkArm.getSuccessfulNetworks();
                for (int i = 0; i < nets.length; i++) {
                    StationDbObject[] stas = networkArm.getSuccessfulStations(nets[i]);
                    for (int j = 0; j < stas.length; j++) {
                        SiteDbObject[] sites = networkArm.getSuccessfulSites(nets[i], stas[j]);
                        for (int k = 0; k < sites.length; k++){
                            networkArm.getSuccessfulChannels(nets[i], sites[k]);
                        }
                    }
                }

            } catch (Exception e) {
                GlobalExceptionHandler.handle(e);
            }
        }


        for (int i = 0; i < pairIds.length; i++) {
            WaveformWorkUnit workUnit = null;
            if (localSeismogramArm != null){
                workUnit =
                    new LocalSeismogramWaveformWorkUnit(pairIds[i]);
            }
            else{
                try {
                    EventChannelPair ecp = evChanStatus.get(pairIds[i],this);
                    EventChannelGroupPair ecgp = getEventChannelGroupPair(ecp);
                    if (!usedPairGroups.contains(ecgp)){
                        usedPairGroups.add(ecgp);
                        int[] pairGroup = evChanStatus.getPairs(ecgp);
                        workUnit =
                            new MotionVectorWaveformWorkUnit(pairGroup);
                    }
                } catch (NotFound e) {
                    GlobalExceptionHandler.handle("EventChannelStatus table unable to find pair " +
                                                      pairIds[i] + " right after it gave it to me",
                                                  e);
                }
            }
            if (workUnit != null){
                //                System.out.println("putting workunit for pairId "
                //                                  + pairIds[i] + " in pool");
                pool.invokeLater(workUnit);
            }
        }
    }

    private int getNumRetryWaiting() {
        synchronized(retryNumLock){ return retryNum; }
    }

    /**
     * This method blocks until there is space in the pool for wu to run, then
     * starts its execution.
     */
    private void invokeLaterAsCapacityAllows(WaveformWorkUnit wu){
        while(pool.getNumWaiting() > poolLineCapacity){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        pool.invokeLater(wu);
    }

    private void waitForInitialEvent() throws SQLException {
        int next;
        synchronized(eventStatus) {
            next = eventStatus.getNext();
        }
        while(Start.getEventArm().isAlive() && next == -1){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            synchronized(eventStatus) {
                next = eventStatus.getNext();
            }
        }
    }

    /**
     * processes the configuration file checking for waveformArmSubsetter types.
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
                Element el = (Element)node;
                if (el.getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load(el,"waveformArm");
                if(sodElement instanceof EventStationSubsetter){
                    eventStationSubsetter = (EventStationSubsetter)sodElement;
                }else if(sodElement instanceof LocalSeismogramArm){
                    localSeismogramArm = (LocalSeismogramArm)sodElement;
                }else if(sodElement instanceof MotionVectorArm){
                    motionVectorArm = (MotionVectorArm)sodElement;
                }else if(sodElement instanceof WaveformArmMonitor){
                    addStatusMonitor((WaveformArmMonitor)sodElement);
                }else {
                    System.err.println("Unknown tag "+el.getTagName()+" found in config file");
                    throw new IllegalArgumentException("The waveformArm does not know about tag " + el.getTagName());
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)

    }

    public void addStatusMonitor(WaveformArmMonitor monitor){
        statusMonitors.add(monitor);
    }

    public synchronized void setStatus(EventChannelPair ecp){
        synchronized(evChanStatus){
            try {
                evChanStatus.setStatus(ecp.getPairId(), ecp.getStatus());
                Status stat = ecp.getStatus();
                if(stat.getStanding() == Standing.CORBA_FAILURE){
                    handleCorbaFailure(ecp);
                }else if(stat.getStanding() == Standing.RETRY){
                    handleAvailableDataFailure(ecp);
                }
            } catch (SQLException e) {
                GlobalExceptionHandler.handle("Trouble setting the status on an event channel pair",
                                              e);
            }
        }
        synchronized(statusMonitors){
            Iterator it = statusMonitors.iterator();
            while(it.hasNext()){
                try {
                    ((WaveformArmMonitor)it.next()).update(ecp);
                } catch (Exception e) {
                    // oh well, log it and go to next status processor
                    GlobalExceptionHandler.handle("Problem in setStatus", e);
                }
            }
        }
    }

    private void handleCorbaFailure(EventChannelPair ecp) throws SQLException{
        synchronized (eventRetryTable) {
            eventRetryTable.addRetry(ecp.getPairId());
        }
    }

    private void handleAvailableDataFailure(EventChannelPair ecp) throws SQLException {
        MicroSecondDate oTime =
            new MicroSecondDate(ecp.getEvent().getOrigin().origin_time);
        if (oTime.after(ClockUtil.now().subtract(MAX_RETRY_DELAY))) {
            synchronized (eventRetryTable) {
                eventRetryTable.addRetry(ecp.getPairId());
            }
        }else{
            ecp.update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                  Standing.REJECT));
        }
    }

    private EventDbObject popAndGet(){
        synchronized(eventStatus){
            try {
                int id = eventStatus.getNext();
                if(id != -1) return getEvent(id);
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Trouble with event db", e);
            }
        }
    }

    private EventDbObject getEvent(int eventDbId){
        synchronized(eventStatus){
            try {
                return new EventDbObject(eventDbId,
                                         eventStatus.getEvent(eventDbId));
            } catch (Exception e) {
                throw new RuntimeException("Trouble with event db", e);
            }
        }
    }

    private interface WaveformWorkUnit extends Runnable{}

    private class LocalSeismogramWaveformWorkUnit implements WaveformWorkUnit {
        public LocalSeismogramWaveformWorkUnit(int pairId){
            this.pairId = pairId;
        }

        public void run(){
            try{
                ecp = extractEventChannelPair();
                ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                      Standing.IN_PROG));
                boolean accepted = false;
                try {
                    Station evStation = ecp.getChannel().my_site.my_station;
                    accepted = eventStationSubsetter.accept(ecp.getEvent(),
                                                            evStation,
                                                            ecp.getCookieJar());
                } catch (Throwable e) {
                    ecp.update(e, Status.get(Stage.EVENT_STATION_SUBSETTER,
                                             Standing.SYSTEM_FAILURE));
                    return;
                }
                if(accepted){
                    ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                          Standing.IN_PROG));
                    localSeismogramArm.processLocalSeismogramArm(ecp);
                }else{
                    ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                          Standing.REJECT));
                }
            }catch(Throwable t){
                System.err.println(BIG_ERROR_MSG);
                t.printStackTrace(System.err);
                GlobalExceptionHandler.handle(BIG_ERROR_MSG, t);
            }
        }

        private EventChannelPair extractEventChannelPair() throws Exception{
            try {
                synchronized(evChanStatus){
                    return evChanStatus.get(pairId, WaveformArm.this);
                }
            } catch (NotFound e) {
                throw new RuntimeException("Not found getting the event and channel ids from the event channel status db for a just gotten pair id.  This shouldn't happen.", e);
            } catch (SQLException e) {
                throw new RuntimeException("SQL Exception getting the event and channel ids from the event channel status db", e);
            }
        }

        protected EventChannelPair ecp;
        protected int pairId;
    }


    private class RetryWaveformWorkUnit extends LocalSeismogramWaveformWorkUnit{
        public RetryWaveformWorkUnit(int pairId){
            super(pairId);
            logger.debug("Retrying on pair id " + pairId);
            synchronized(retryNumLock){  retryNum++; }
        }

        public void run(){
            synchronized(retryNumLock){
                retryNum--;
            }
            super.run();
        }
    }


    private class RetryMotionVectorWaveformWorkUnit extends MotionVectorWaveformWorkUnit {

        public RetryMotionVectorWaveformWorkUnit(int[] pairId){
            super(pairId);
            logger.debug("Retrying on pair id " + pairId[0]+" "+pairId[1]+" "+pairId[2]);
            synchronized(retryNumLock){  retryNum++; }
        }

        public void run(){
            synchronized(retryNumLock){
                retryNum--;
            }
            super.run();
        }
    }

    private class MotionVectorWaveformWorkUnit implements WaveformWorkUnit {

        public MotionVectorWaveformWorkUnit(int[] pairIds){
            this.pairIds = pairIds;
        }

        public void run(){
            try{
                EventChannelGroupPair ecp = extractEventChannelGroupPair();
                ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                      Standing.IN_PROG));
                boolean accepted = false;
                try {
                    Station evStation = ecp.getChannelGroup().getChannels()[0].my_site.my_station;
                    accepted = eventStationSubsetter.accept(ecp.getEvent(),
                                                            evStation,
                                                            ecp.getCookieJar());
                } catch (Throwable e) {
                    ecp.update(e, Status.get(Stage.EVENT_STATION_SUBSETTER,
                                             Standing.SYSTEM_FAILURE));
                    return;
                }
                if(accepted){
                    ecp.update(Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                          Standing.IN_PROG));
                    motionVectorArm.processMotionVectorArm(ecp);
                }else{
                    ecp.update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                          Standing.REJECT));
                }
            }catch(Throwable t){
                System.err.println(BIG_ERROR_MSG);
                t.printStackTrace(System.err);
                GlobalExceptionHandler.handle(BIG_ERROR_MSG, t);
            }
        }

        private EventChannelGroupPair extractEventChannelGroupPair() throws Exception{
            try {
                EventChannelPair[] pairs = new EventChannelPair[pairIds.length];
                synchronized(evChanStatus){
                    for (int i = 0; i < pairIds.length; i++) {
                        pairs[i] = evChanStatus.get(pairIds[i], WaveformArm.this);
                    }
                }
                return new EventChannelGroupPair(pairs);
            } catch (NotFound e) {
                throw new RuntimeException("Not found getting the event and channel ids from the event channel status db for a just gotten pair id.  This shouldn't happen.", e);
            } catch (SQLException e) {
                throw new RuntimeException("SQL Exception getting the event and channel ids from the event channel status db", e);
            }
        }

        private int[] pairIds;
    }

    private static final String BIG_ERROR_MSG = "An exception occured that would've croaked a waveform worker thread!  These types of exceptions are certainly possible, but they shouldn't be allowed to percolate this far up the stack.  If you are one of those esteemed few working on SOD, it behooves you to attempt to trudge down the stack trace following this message and make certain that whatever threw this exception is no longer allowed to throw beyond its scope.  If on the other hand, you are a user of SOD it would be most appreciated if you would send an email containing the text immediately following this mesage to sod@seis.sc.edu";

    private boolean finished = false;

    private WorkerThreadPool pool;

    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();

    private LocalSeismogramArm localSeismogramArm = null;

    private MotionVectorArm motionVectorArm = null;

    private NetworkArm networkArm = null;

    private JDBCEventStatus eventStatus;
    private JDBCEventChannelStatus evChanStatus;
    private JDBCEventChannelRetry eventRetryTable;

    private ChannelGrouper channelGrouper = new ChannelGrouper();
    private List usedPairGroups = new ArrayList();

    private double retryPercentage = .02;//2 percent of the pool will be
    //made up of retries if possible

    /** Maxmimun time back from now that it is worth retrying. */
    private TimeInterval MAX_RETRY_DELAY;

    //Amount of time after the run has ended that we retry Server based failures
    private TimeInterval SERVER_RETRY_DELAY ;

    private static Logger logger = Logger.getLogger(WaveformArm.class);

    private Set statusMonitors = Collections.synchronizedSet(new HashSet());
    private int poolLineCapacity = 100, retryNum;
    private Object retryNumLock = new Object();
}




