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
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.MapPool;
import edu.sc.seis.sod.status.PeriodicAction;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

public class MapWaveformStatus extends PeriodicAction {
    public MapWaveformStatus() throws SQLException{
        this(new MapPool(1, new DefaultEventColorizer()));
    }

    public MapWaveformStatus(MapPool pool) throws SQLException{
        this.pool = pool;
        evChanStatusTable = new JDBCEventChannelStatus();
    }
    public void act() {
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
                EventChannelPair[] ecps = evChanStatusTable.getAll(events[i]);
                for (int j = 0; j < ecps.length; j++) {
                    Station station = ecps[j].getChannel().my_site.my_station;
                    Station[] stations = {station};
                    sl.stationDataChanged(new StationDataEvent(this, stations));
                    AvailableStationDataEvent availability = null;
                    Status status = ecps[j].getStatus();
                    if (status.getStanding() == Standing.REJECT||
                        status.getStanding() == Standing.CORBA_FAILURE||
                        status.getStanding() == Standing.SYSTEM_FAILURE){
                        availability = new AvailableStationDataEvent(this,
                                                                     station,
                                                                     AvailableStationDataEvent.DOWN);
                    }else if (status.getStanding() == Standing.SUCCESS||
                              status.getStanding() == Standing.IN_PROG||
                              status.getStanding() == Standing.RETRY){
                        availability = new AvailableStationDataEvent(this,
                                                                     station,
                                                                     AvailableStationDataEvent.UP);
                    }else{
                        availability = new AvailableStationDataEvent(this,
                                                                     station,
                                                                     AvailableStationDataEvent.UNKNOWN);
                    }
                    sl.stationAvailabiltyChanged(availability);
                }
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
                                } catch (IOException e) {
                                    GlobalExceptionHandler.handle("unable to save map to "+fileLoc, e);
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

    public void write(){ actIfPeriodElapsed();  }

    public boolean add(EventAccessOperations ev, String outputLoc){
        synchronized(eventsToBeRendered){
            if (eventsToBeRendered.containsKey(ev)){ return false; }
            eventsToBeRendered.put(ev, outputLoc);
            write();
            return true;
        }
    }

    private Map eventsToBeRendered = Collections.synchronizedMap(new HashMap());
    private MapPool pool;
    private JDBCEventChannelStatus evChanStatusTable;
    private static Logger logger = Logger.getLogger(MapWaveformStatus.class);
}
