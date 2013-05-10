package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.EventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.ShapeLayerPropertiesHandler;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;

public class MapPool {

    public MapPool(int mapCount) {
        maps = new OpenMap[mapCount];
        free = new boolean[mapCount];
        for(int i = 0; i < maps.length; i++) {
            maps[i] = new OpenMap(ShapeLayerPropertiesHandler.getProperties());
            maps[i].setEtopoLayer("edu/sc/seis/mapData");
            maps[i].setStationLayer(new StationLayer());
            maps[i].overrideProjChangedInOMLayers(true);
            free[i] = true;
        }
    }

    public OpenMap getMap(EventColorizer colorizer) {
        while(true) {
            for(int i = 0; i < maps.length; i++) {
                synchronized(free) {
                    if(free[i]) {
                        free[i] = false;
                        maps[i].setEventLayer(new EventLayer(maps[i], colorizer));
                        return maps[i];
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {}
        }
    }

    public void returnMap(OpenMap map) {
        clear(map);
        for(int i = 0; i < maps.length; i++)
            if(maps[i] == map)
                synchronized(free) {
                    free[i] = true;
                }
    }

    private void clear(OpenMap map) {
        map.getEventLayer().eventDataCleared();
        map.getStationLayer().stationDataCleared();
    }

    public static MapPool getDefaultPool() {
        synchronized(MapPool.class) {
            if(defaultPool == null) {
                defaultPool = new MapPool(1);
            }
            return defaultPool;
        }
    }

    private static MapPool defaultPool;

    private boolean[] free;

    private OpenMap[] maps;
}
