package edu.sc.seis.sod.status.eventArm;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.FreshnessEventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.MapPool;
import edu.sc.seis.sod.status.OutputScheduler;

public class MapEventStatus implements SodElement, EventMonitor, Runnable {

    private String fileLoc;

    public MapEventStatus(Element element) {
        this(getLocation(element));
    }

    public MapEventStatus(String location) {
        fileLoc = location;
        run();
    }

    public static String getLocation(Element el) {
        return FileWritingTemplate.getBaseDirectoryName() + '/'
                + el.getAttribute("xlink:href");
    }

    public void change(CacheEvent event, Status status) {
        OutputScheduler.getDefault().schedule(this);
    }

    public String getLocation() {
        return fileLoc;
    }

    public void run() {
        OpenMap map = pool.getMap(new FreshnessEventColorizer());
        try {
            EventLayer el = map.getEventLayer();
            el.eventDataChanged(new EQDataEvent(StatefulEventDB.getSingleton().getAll()));
            map.writeMapToPNG(fileLoc);
        } catch(IOException e) {
            throw new RuntimeException("Trouble writing map", e);
        } finally {
            pool.returnMap(map);
        }
    }

    public void setArmStatus(String status) {}// noImpl

    private static MapPool pool = MapPool.getDefaultPool();

    private static Logger logger = Logger.getLogger(MapEventStatus.class);
}