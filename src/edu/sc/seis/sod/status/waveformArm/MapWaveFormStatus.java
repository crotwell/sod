/**
 * MapWaveFormStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.chooser.AvailableStationDataEvent;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.DefaultEventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.MapPool;
import edu.sc.seis.sod.status.PeriodicAction;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class MapWaveformStatus extends PeriodicAction implements WaveformArmMonitor {

    private String fileLoc;
    private List events = new ArrayList();
    private Map channelMap = new HashMap();
    private MapPool pool;

    public MapWaveformStatus(Element element) {
        this(element.getAttribute("xlink:href"));
    }

    public MapWaveformStatus(String fileLoc){
        this(fileLoc, new MapPool(1, new DefaultEventColorizer()));
    }

    public MapWaveformStatus(String fileLoc, MapPool pool){
        this.fileLoc = fileLoc;
        this.pool = pool;
        write();
    }
    public void act() {
        try{
            synchronized(channelMap){
                final OpenMap map = pool.getMap();
                StationLayer sl = map.getStationLayer();
                sl.honorRepaint(false);
                Iterator it = channelMap.keySet().iterator();
                while(it.hasNext()){
                    Channel cur = (Channel)it.next();
                    sl.stationDataChanged(new StationDataEvent(this, new Station[]{cur.my_site.my_station}));
                    Status status = (Status)channelMap.get(cur);
                    if (status.getType() == Status.REJECT||
                        status.getType() == Status.CORBA_FAILURE||
                        status.getType() == Status.SYSTEM_FAILURE){
                        sl.stationAvailabiltyChanged(new AvailableStationDataEvent(this,
                                                                                   cur.my_site.my_station,
                                                                                   AvailableStationDataEvent.DOWN));
                    }
                    else if (status.getType() == Status.SPECIAL||
                             status.getType() == Status.IN_PROG||
                             status.getType() == Status.RETRY){
                        sl.stationAvailabiltyChanged(new AvailableStationDataEvent(this,
                                                                                   cur.my_site.my_station,
                                                                                   AvailableStationDataEvent.UP));
                    }
                    else{
                        sl.stationAvailabiltyChanged(new AvailableStationDataEvent(this,
                                                                                   cur.my_site.my_station,
                                                                                   AvailableStationDataEvent.UNKNOWN));
                    }
                }
                sl.honorRepaint(true);
                EventLayer el = map.getEventLayer();
                synchronized(events){
                    it = events.iterator();
                    while(it.hasNext()){
                        EventAccessOperations ev = (EventAccessOperations)it.next();
                        el.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{ev}));
                    }
                }
                SwingUtilities.invokeAndWait(new Runnable(){
                            public void run(){
                                try{
                                    map.writeMapToPNG(fileLoc);
                                } catch (IOException e) {
                                    CommonAccess.handleException("unable to save map to "+fileLoc, e);
                                }
                            }
                        });

                pool.returnMap(map);
            }
        }catch(Throwable t){
            CommonAccess.handleException(t, "Waveform map updater had a problem");
        }
    }


    public void write(){ actIfPeriodElapsed();  }

    public void update(EventChannelPair ecp) {
        if(add(ecp.getEvent())){
            add(ecp.getChannel(), ecp.getStatus());
            write();
        }else if(add(ecp.getChannel(), ecp.getStatus()))
            write();
    }

    public boolean add(Channel chan, Status status){
        synchronized(channelMap){
            if(channelMap.put(chan, status) != status) return true;
        }
        return false;
    }

    public boolean add(EventAccessOperations ev){
        if (events.contains(ev)) return false;
        synchronized(events){
            events.add(ev);
        }
        return true;
    }

    public boolean contains(Channel chan){ return channelMap.containsKey(chan);}

    public Status getStatus(Channel chan){ return (Status)channelMap.get(chan);}

    public boolean contains(EventAccessOperations ev) {
        return events.contains(ev);
    }

    private static Logger logger = Logger.getLogger(MapWaveformStatus.class);
}

