/**
 * MapWaveFormStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import java.util.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.chooser.AvailableStationDataEvent;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import edu.sc.seis.sod.subsetter.MapPool;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class MapWaveFormStatus implements WaveFormStatus {

    private String fileLoc;
    private List events = new ArrayList();
    private Map channelMap = new HashMap();
    private MapPool pool;

    public MapWaveFormStatus(Element element) {
        this(element.getAttribute("xlink:href"));
    }

    public MapWaveFormStatus(String fileLoc){
        this(fileLoc, new MapPool(1));
    }

    public MapWaveFormStatus(String fileLoc, MapPool pool){
        this.fileLoc = fileLoc;
        this.pool = pool;
        write();
    }
    private class MapWriter extends TimerTask{
        public void run() {
            try{
                synchronized(channelMap){
                    OpenMap map = pool.getMap();
                    StationLayer sl = map.getStationLayer();
                    sl.honorRepaint(false);
                    Iterator it = channelMap.keySet().iterator();
                    while(it.hasNext()){
                        Channel cur = (Channel)it.next();
                        sl.stationDataChanged(new StationDataEvent(this, new Station[]{cur.my_site.my_station}));
                        EventChannelCondition status = (EventChannelCondition)channelMap.get(cur);
                        if (status == EventChannelCondition.FAILURE ||
                            status == EventChannelCondition.PROCESSOR_FAILED||
                            status == EventChannelCondition.SUBSETTER_FAILED){
                            sl.stationAvailabiltyChanged(new AvailableStationDataEvent(this,
                                                                                       cur.my_site.my_station,
                                                                                       AvailableStationDataEvent.DOWN));
                        }
                        else if (status == EventChannelCondition.SUCCESS ||
                                status == EventChannelCondition.PROCESSOR_PASSED ||
                                status == EventChannelCondition.SUBSETTER_PASSED){
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

                    try{
                        map.writeMapToPNG(fileLoc);
                    } catch (IOException e) {
                        CommonAccess.handleException("unable to save map to "+fileLoc, e);
                    }
                    pool.returnMap(map);
                    scheduled = new Boolean(false);
                }
            }catch(Throwable t){
                CommonAccess.handleException(t, "Waveform map updater had a problem");
            }
        }
    }

    private static Timer t = new Timer();

    private Boolean scheduled = new Boolean(false);

    public void write(){
        synchronized(scheduled){
            if(scheduled.equals(Boolean.FALSE)){
                t.schedule(new MapWriter(), 120 * 1000);
                scheduled = new Boolean(true);
            }
        }
    }

    public void update(EventChannelPair ecp) {
        if(add(ecp.getEvent())){
            add(ecp.getChannel(), ecp.getStatus());
            write();
        }else if(add(ecp.getChannel(), ecp.getStatus()))
            write();
    }

    public boolean add(Channel chan, EventChannelCondition status){
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

    public EventChannelCondition getStatus(Channel chan){ return (EventChannelCondition)channelMap.get(chan);}

    public boolean contains(EventAccessOperations ev) {
        return events.contains(ev);
    }

    private static Logger logger = Logger.getLogger(MapWaveFormStatus.class);
}

