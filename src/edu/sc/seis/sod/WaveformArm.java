package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
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
import edu.sc.seis.sod.database.network.JDBCNetworkUnifier;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelRetry;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import edu.sc.seis.sod.subsetter.waveformArm.EventEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.waveformArm.NullEventStationSubsetter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
    }

    public void run() {
        try {
            waitForInitialEvent();
            do{
                populateEventChannelDb();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {}
                retryIfNeededAndAvailable();
            } while(Start.getEventArm().isAlive());
            logger.info("Waveform arm done.");
        } catch(Throwable e) {
            CommonAccess.handleException("Problem running waveform arm", e);
        }
    }

    //fills the eventchannel db with all available events and starts
    //WaveformWorkerUnits on all inserted event channel pairs
    //If there are no waiting events, this just returns
    private void populateEventChannelDb()throws Exception{
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
            synchronized(eventStatus){
                eventStatus.setStatus(ev.getEvent(),Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                               Standing.SUCCESS));
            }
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
        for(int i = 0; i < chans.length; i++) {
            startChannel(overlap, chans[i], ev);
        }
    }

    private void startChannel(EventEffectiveTimeOverlap overlap, ChannelDbObject chan, EventDbObject ev)throws Exception{
        if ( !overlap.overlaps(chan.getChannel())) {
            logger.debug("The channel effective time does not overlap the event time");
            return;
        } // end of if ()
        //cache the channelInformation.
        int pairId;
        synchronized(evChanStatus){
            pairId = evChanStatus.put(ev.getDbId(), chan.getDbId(),
                                      Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                 Standing.INIT));
        }
        invokeLaterAsCapacityAllows(new WaveformWorkUnit(pairId));
        retryIfNeededAndAvailable();
    }

    private void retryIfNeededAndAvailable() throws SQLException {
        if(getNumRetryWaiting()/(double)pool.getNumWaiting() < retryPercentage){
            retryIfAvailable();
        }
    }

    private void retryIfAvailable() throws SQLException{
        WaveformWorkUnit retryUnit = getNextRetry();
        if(retryUnit != null)invokeLaterAsCapacityAllows(retryUnit);
    }

    private WaveformWorkUnit getNextRetry() throws SQLException {
        int pairId;
        synchronized (eventRetryTable) {
            pairId = eventRetryTable.next();
        }
        if(pairId != -1) return new RetryWaveformWorkUnit(pairId);
        return null;
    }

    private int getNumRetryWaiting() {
        synchronized(retryNumLock){ return retryNum; }
    }

    private class RetryWaveformWorkUnit extends WaveformWorkUnit{
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
        logger.debug("Updating status on " + ecp);
        synchronized(evChanStatus){
            try {
                evChanStatus.setStatus(ecp.getPairId(), ecp.getStatus());
                Status stat = ecp.getStatus();
                if(stat.getStanding() == Standing.CORBA_FAILURE ||
                       (stat.getStage() == Stage.AVAILABLE_DATA_SUBSETTER &&
                            stat.getStanding() == Standing.REJECT)){
                    synchronized (eventRetryTable) {
                        eventRetryTable.failed(ecp.getPairId(), ecp.getStatus());
                    }
                }
            } catch (SQLException e) {
                CommonAccess.handleException("Trouble setting the status on an event channel pair",
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
                    CommonAccess.handleException("Problem in setStatus", e);
                }
            }
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

    private class WaveformWorkUnit implements Runnable{
        public WaveformWorkUnit(int pairId){
            this.pairId = pairId;
        }

        public void run(){
            try{
                EventChannelPair ecp = extractEventChannelPair();
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
                System.err.println("An exception occured that would've croaked a waveform worker thread!  These types of exceptions are certainly possible, but they shouldn't be allowed to percolate this far up the stack.  If you are one of those esteemed few working on SOD, it behooves you to attempt to trudge down the stack trace following this message and make certain that whatever threw this exception is no longer allowed to throw beyond its scope.  If on the other hand, you are a user of SOD it would be most appreciated if you would send an email containing the text immediately following this mesage to sod@seis.sc.edu");
                t.printStackTrace(System.err);
                GlobalExceptionHandler.handle(t);
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
        private int pairId;
    }

    private WorkerThreadPool pool;

    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm = null;

    private JDBCEventStatus eventStatus;
    private JDBCEventChannelStatus evChanStatus;
    private JDBCEventChannelRetry eventRetryTable;

    private double retryPercentage = .02;//2 percent of the pool will be
    //made up of retries if possible
    private static Logger logger = Logger.getLogger(WaveformArm.class);

    private List statusMonitors = Collections.synchronizedList(new ArrayList());
    private int poolLineCapacity = 100, retryNum;
    private Object retryNumLock = new Object();
}



