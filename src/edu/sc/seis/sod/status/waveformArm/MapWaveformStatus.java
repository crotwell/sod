package edu.sc.seis.sod.status.waveformArm;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import com.bbn.openmap.event.CenterEvent;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.chooser.AvailableStationDataEvent;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.DefaultEventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.DistanceLayer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.MapPool;
import edu.sc.seis.sod.status.OutputScheduler;

public class MapWaveformStatus implements Runnable{
    public MapWaveformStatus() throws SQLException{
        this(new MapPool(1, new DefaultEventColorizer()));
    }

    public MapWaveformStatus(MapPool pool) throws SQLException{
        this.pool = pool;
        evChanStatusTable = new JDBCEventChannelStatus();
    }

    public void run() {
        int numEventsWaiting = 0;
        ProxyEventAccessOperations[] events = new ProxyEventAccessOperations[0];
        String[] fileLocs = new String[0];
        synchronized(eventsToBeRendered){
            numEventsWaiting = eventsToBeRendered.size();
            if(eventsToBeRendered.size()>0){
                events = new ProxyEventAccessOperations[eventsToBeRendered.size()];
                fileLocs = new String[eventsToBeRendered.size()];
                Iterator it = eventsToBeRendered.keySet().iterator();
                while(it.hasNext()){
                    ProxyEventAccessOperations cur = (ProxyEventAccessOperations)it.next();
                    events[--numEventsWaiting] = cur;
                    fileLocs[numEventsWaiting] = (String)eventsToBeRendered.get(cur);
                }
                eventsToBeRendered.clear();
            }
        }
        final OpenMap map = pool.getMap();
        try{
            for (int i = 0; i < events.length; i++) {
                StationLayer sl = map.getStationLayer();
                sl.honorRepaint(false);
                Station[] unsuccessful = evChanStatusTable.getNotOfStatus(success, events[i]);
                addStations(sl, unsuccessful, AvailableStationDataEvent.DOWN);
                Station[] successful = evChanStatusTable.getOfStatus(success, events[i]);
                addStations(sl, successful, AvailableStationDataEvent.UP);
                sl.honorRepaint(true);
                EventLayer el = map.getEventLayer();
                DistanceLayer dl = map.getDistanceLayer();
                EQDataEvent eqEvent = new EQDataEvent(new ProxyEventAccessOperations[]{events[i]});
                el.eventDataChanged(eqEvent);
                EQSelectionEvent selEvent = new EQSelectionEvent(this, new ProxyEventAccessOperations[]{events[i]});
                Origin orig = EventUtil.extractOrigin(events[i]);
                map.getMapBean().center(new CenterEvent(this, 0.0f, orig.my_location.longitude));
                dl.eqSelectionChanged(selEvent);
                final String fileLoc = fileLocs[i];
                SwingUtilities.invokeAndWait(new Runnable(){
                            public void run(){
                                try{
                                    map.writeMapToPNG(fileLoc);
                                } catch (Throwable e) {
                                    GlobalExceptionHandler.handle("problem writing map", e);
                                }
                            }
                        });
                sl.stationDataCleared();
                el.eventDataCleared();
            }
        }catch(Throwable t){
            GlobalExceptionHandler.handle("Waveform map updater had a problem", t);
        }
        pool.returnMap(map);
    }

    private static void addStations(StationLayer sl, Station[] stations, int status){
        sl.stationDataChanged(new StationDataEvent(stations));
        for (int j = 0; j < stations.length; j++) {
            sl.stationAvailabiltyChanged(new AvailableStationDataEvent(stations[j],
                                                                       status));
        }
    }

    public boolean add(EventAccessOperations ev, String outputLoc){
        synchronized(eventsToBeRendered){
            if (eventsToBeRendered.containsKey(ev)){ return false; }
            eventsToBeRendered.put(ev, outputLoc);
            OutputScheduler.getDefault().schedule(this);
            return true;
        }
    }

    private static final Status success = Status.get(Stage.PROCESSOR,Standing.SUCCESS);
    private Map eventsToBeRendered = Collections.synchronizedMap(new HashMap());
    private MapPool pool;
    private JDBCEventChannelStatus evChanStatusTable;
    private static Logger logger = Logger.getLogger(MapWaveformStatus.class);
}
