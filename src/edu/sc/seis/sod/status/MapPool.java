package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.EventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.ShapeLayerPropertiesHandler;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;

public class MapPool{
    public MapPool(int mapCount, EventColorizer colorizer){
        maps = new OpenMap[mapCount];
        free = new boolean[mapCount];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = 
            	new OpenMap(ShapeLayerPropertiesHandler.getProperties());
            maps[i].setEtopoLayer("edu/sc/seis/mapData");
            maps[i].setEventLayer(new EventLayer(maps[i], colorizer));
            maps[i].setStationLayer(new StationLayer());
            maps[i].overrideProjChangedInOMLayers(true);
            free[i] = true;
        }
    }
    
    public OpenMap getMap(){
        while(true){
            for (int i = 0; i < maps.length; i++) {
                synchronized(free){
                    if(free[i]) {
                        free[i] = false;
                        return maps[i];
                    }
                }
            }
            try { Thread.sleep(1000);} catch (InterruptedException e) {}
        }
    }
    
    public void returnMap(OpenMap map){
        clear(map);
        for (int i = 0; i < maps.length; i++)
            if(maps[i] == map) synchronized(free){free[i] = true;}
        
    }
    
    private void clear(OpenMap map){
        map.getEventLayer().eventDataCleared();
        map.getStationLayer().stationDataCleared();
    }
    
    private boolean[] free;
    private OpenMap[] maps;
}
