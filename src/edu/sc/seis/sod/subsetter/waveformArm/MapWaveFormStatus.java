/**
 * MapWaveFormStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

public class MapWaveFormStatus implements WaveFormStatus {
    
    private OpenMap mainMap = new OpenMap("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");
    private EventLayer eventLayer;
    private StationLayer stationLayer;
    private String fileLoc;
    private Map eventMap = new HashMap();
    private Map channelMap = new HashMap();
    
    public MapWaveFormStatus(Element element) {
        this(element.getAttribute("xlink:href"));
    }
    
    public MapWaveFormStatus(String fileLoc){
        this.fileLoc = fileLoc;
        eventLayer = new EventLayer(mainMap.getMapBean());
        mainMap.setEventLayer(eventLayer);
        stationLayer = new StationLayer();
        mainMap.setStationLayer(stationLayer);
        write();
    }
    
    public void write(){ mainMap.writeMapToPNG(fileLoc); }
    
    /**
     * Method update
     *
     * @param    ecp                 an EventChannelPair
     *
     */
    public void update(EventChannelPair ecp) {
        if(add(ecp.getEvent())){
            add(ecp.getChannel());
            write();
        }else if(add(ecp.getChannel()))
            write();
    }
    
    public boolean add(Channel chan){
        if (!channelMap.containsKey(chan)){
            stationLayer.stationDataChanged(new StationDataEvent(this, new Station[]{chan.my_site.my_station}));
            channelMap.put(chan, null);
            return true;
        }
        return false;
    }
    public boolean add(EventAccessOperations ev){
        if (!eventMap.containsKey(ev)){
            eventLayer.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{ev}));
            eventMap.put(ev, null);
                    return true;
                    
        }
        return false;
    }
    
//    public static void main(String[] args){
//        MapWaveFormStatus mwfs = new MapWaveFormStatus("mwfsTest.png");
//
//        MockFissures mf = new MockFissures();
//        mwfs.add(mf.createFallEvent());
//
//        mwfs.write();
//        System.exit(0);
//    }
}

