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
        fileLoc = element.getAttribute("xlink:link");
        eventLayer = new EventLayer(mainMap.getMapBean());
        mainMap.setEventLayer(eventLayer);
        stationLayer = new StationLayer();
        mainMap.setStationLayer(stationLayer);
    }
    
    /**
     * Method update
     *
     * @param    ecp                 an EventChannelPair
     *
     */
    public void update(EventChannelPair ecp) {
        EventAccessOperations ecpEvent = ecp.getEvent();
        Channel ecpChannel = ecp.getChannel();
        
        if (!eventMap.containsKey(ecpEvent)){
            eventLayer.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{ecpEvent}));
            eventMap.put(ecpEvent, null);
        }
        if (!channelMap.containsKey(ecpChannel)){
            stationLayer.stationDataChanged(new StationDataEvent(this, new Station[]{ecpChannel.my_site.my_station}));
            channelMap.put(ecpChannel, null);
        }
        
        mainMap.writeMapToPNG(fileLoc);
    }
    
    //    private OpenMap getMapForEvent(EventAccessOperations event){
    //        Iterator it = eventMapMap.keySet().iterator();
    //        OpenMap eventMap = null;
    //
    //        while (it.hasNext()){
    //            try {
    //                EventAccessOperations curEvent = (EventAccessOperations)it.next();
    //                if (DisplayUtils.originIsEqual(curEvent, event)){
    //                    eventMap = (OpenMap)eventMapMap.get(curEvent);
    //                }
    //            }
    //            catch (NoPreferredOrigin e) {
    //                e.printStackTrace();
    //            }
    //        }
    //
    //        if (eventMap == null){
    //            eventMap = new OpenMap("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");
    //            EventLayer evLayer = new EventLayer(eventMap.getMapBean());
    //            eventMap.setEventLayer(evLayer);
    //            StationLayer staLayer = new StationLayer();
    //            eventMap.setStationLayer(staLayer);
    //            eventMapMap.put(event, eventMap);
    //        }
    //
    //        return eventMap;
    //    }
}

