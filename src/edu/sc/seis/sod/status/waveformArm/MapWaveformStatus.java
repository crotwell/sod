package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.AvailableStationDataEvent;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.DefaultEventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.MapPool;
import edu.sc.seis.sod.status.OutputScheduler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

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
        CacheEvent[] events = new CacheEvent[0];
        String[] fileLocs = new String[0];
        synchronized(eventsToBeRendered){
            numEventsWaiting = eventsToBeRendered.size();
            if(eventsToBeRendered.size()>0){
                events = new CacheEvent[eventsToBeRendered.size()];
                fileLocs = new String[eventsToBeRendered.size()];
                Iterator it = eventsToBeRendered.keySet().iterator();
                while(it.hasNext()){
                    CacheEvent cur = (CacheEvent)it.next();
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
                EQDataEvent eqEvent = new EQDataEvent(this,
                                                      new EventAccessOperations[]{events[i]});
                el.eventDataChanged(eqEvent);
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
