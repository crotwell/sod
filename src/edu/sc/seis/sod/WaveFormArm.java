package edu.sc.seis.sod;

import java.util.*;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.SiteDbObject;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.database.event.EventCondition;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.subsetter.waveFormArm.EventEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.waveFormArm.LocalSeismogramArm;
import edu.sc.seis.sod.subsetter.waveFormArm.NullEventStationSubsetter;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WaveFormArm implements Runnable {
    public WaveFormArm(Element config, NetworkArm networkArm)
        throws Exception {
        this(config, networkArm, 5);
    }
    
    public WaveFormArm(Element config, NetworkArm networkArm, int threadPoolSize)
        throws Exception {
        eventStatus = new JDBCEventStatus();
        evChanStatus = new JDBCEventChannelStatus();
        processConfig(config);
        this.networkArm = networkArm;
        pool = new WorkerThreadPool("Waveform EventChannel Processor", threadPoolSize);
    }
    
    public void run() {
        try {
            waitForInitialEvent();
            while(Start.getEventArm().isAlive()){
                populateEventChannelDb();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
            logger.info("Waveform arm done.");
        } catch(Throwable e) {
            CommonAccess.handleException("Problem running waveform arm", e);
        }
    }
    
    private void populateEventChannelDb()throws Exception{
        for(EventDbObject ev = popAndGet(); ev != null; ev = popAndGet()){
            EventEffectiveTimeOverlap overlap =
                new EventEffectiveTimeOverlap(ev.getGetEvent());
            
            NetworkDbObject[] networks = networkArm.getSuccessfulNetworks();
            logger.debug("got " + networks.length + " networks from getSuccessfulNetworks()");
            for(int i = 0; i < networks.length; i++) {
                startNetwork(ev, overlap, networks[i]);
            }
            //set the status of the event to be SUCCESS implying that
            //that all the network information for this particular event is inserted
            //in the waveformDatabase.
            eventStatus.setStatus(ev.getGetEvent(),EventCondition.SUCCESS);
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
            startChannel(chans[i], ev);
        }
    }
    
    private void startChannel(ChannelDbObject chan, EventDbObject ev)throws Exception{
        int chanId = chan.getDbId();
        //cache the channelInformation.
        channelDbCache.put( new Integer(chanId), chan);
        int pairId;
        synchronized(evChanStatus){
            pairId = evChanStatus.put(ev.getDbId(), chanId,
                                      EventChannelCondition.NEW);
        }
        while(pool.getNumWaiting() > 100){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        pool.invokeLater(new WaveformWorkUnit(pairId));
    }
    
    private void waitForInitialEvent() throws SQLException {
        while(Start.getEventArm().isAlive() && eventStatus.getNext() == -1){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
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
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node,"edu.sc.seis.sod.subsetter.waveFormArm");
                if(sodElement instanceof EventStationSubsetter){
                    eventStationSubsetter = (EventStationSubsetter)sodElement;
                }else if(sodElement instanceof LocalSeismogramArm){
                    localSeismogramArm = (LocalSeismogramArm)sodElement;
                }else if(sodElement instanceof WaveFormStatus){
                    addStatusMonitor((WaveFormStatus)sodElement);
                }else {
                    System.err.println("Unkown tag "+((Element)node).getTagName()+" found in config file");
                    System.exit(1);
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
        
    }
    
    public void addStatusMonitor(WaveFormStatus monitor){
        statusMonitors.add(monitor);
    }
    
    public synchronized void signalWaveFormArm()  {
        notifyAll();
    }
    
    
    /**
     * sets the finalStatus of the event. For infomation about the status see
     * the class Status in the package edu.sc.seis.sod.database.
     */
    
    public synchronized void setStatus(EventChannelPair ecp){
        synchronized(evChanStatus){
            try {
                evChanStatus.setStatus(ecp.getPairId(), ecp.getStatus());
            } catch (SQLException e) {
                CommonAccess.handleException("Trouble setting the status on an event channel pair",
                                             e);
            }
        }
        synchronized(statusMonitors){
            Iterator it = statusMonitors.iterator();
            while(it.hasNext()){
                try {
                    ((WaveFormStatus)it.next()).update(ecp);
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
                Integer key = new Integer(eventDbId);
                //TODO - empty cache as it grows
                if(eventDbCache.containsKey(key)){
                    return (EventDbObject)eventDbCache.get(key);
                }
                EventDbObject e = new EventDbObject(eventDbId,
                                                    eventStatus.getEvent(eventDbId));
                eventDbCache.put(key, e);
                return e;
            } catch (Exception e) {
                throw new RuntimeException("Trouble with event db", e);
            }
        }
    }
    
    private class WaveformWorkUnit implements Runnable{
        public WaveformWorkUnit(int pairId) throws SQLException{
            this.pairId = pairId;
            eventStatus = new JDBCEventStatus();
        }
        
        public void run(){
            int[] evAndChanIds = null;
            try {
                synchronized(evChanStatus){
                    evAndChanIds= evChanStatus.getEventAndChanIds(pairId);
                }
            } catch (NotFound e) {
                throw new RuntimeException("Not found getting the event and channel ids from the event channel status db for a just gotten pair id.  This shouldn't happen.", e);
            } catch (SQLException e) {
                throw new RuntimeException("SQL Exception getting the event and channel ids from the event channel status db", e);
            }
            int eventId = evAndChanIds[0];
            int chanId = evAndChanIds[1];
            ChannelDbObject channelDbObject = null;
            Integer key = new Integer(chanId);
            //TODO - empty cache
            if( channelDbCache.containsKey(key)) {
                channelDbObject = (ChannelDbObject)channelDbCache.get(key);
            } else {
                channelDbObject = new ChannelDbObject(chanId, networkArm.getChannel(chanId));
                channelDbCache.put(key, channelDbObject);
            }
            int siteid = networkArm.getSiteDbId(chanId);
            int stationid = networkArm.getStationDbId(siteid);
            int networkid = networkArm.getNetworkDbId(stationid);
            NetworkAccess networkAccess  = networkArm.getNetworkAccess(networkid);
            NetworkDbObject networkDbObject = new NetworkDbObject(networkid, networkAccess);
            WaveFormArmProcessor processor = new WaveFormArmProcessor(getEvent(eventId), eventStationSubsetter,
                                                                      localSeismogramArm, networkDbObject,
                                                                      channelDbObject, WaveFormArm.this,
                                                                      pairId);
            processor.run();
        }
        
        private int pairId;
    }
    
    private WorkerThreadPool pool;
    
    private EventStationSubsetter eventStationSubsetter = new NullEventStationSubsetter();
    
    private LocalSeismogramArm localSeismogramArm = null;
    
    private NetworkArm networkArm = null;
    
    private Map channelDbCache = new HashMap();
    
    private Map eventDbCache = new HashMap();
    
    private JDBCEventStatus eventStatus;
    
    private JDBCEventChannelStatus evChanStatus;
    
    private static Logger logger = Logger.getLogger(WaveFormArm.class);
    
    private List statusMonitors = Collections.synchronizedList(new ArrayList());
}
