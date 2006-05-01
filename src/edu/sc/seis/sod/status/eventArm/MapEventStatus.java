package edu.sc.seis.sod.status.eventArm;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.FreshnessEventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.MapPool;
import edu.sc.seis.sod.status.OutputScheduler;

public class MapEventStatus implements SodElement, EventMonitor, Runnable {

    private String fileLoc;

    public MapEventStatus(Element element) {
        this(getLocation(element));
    }

    public MapEventStatus(String location) {
        try {
            events = new JDBCEventStatus();
        } catch(SQLException e) {
            GlobalExceptionHandler.handle("Trouble creating event status db for use in map",
                                          e);
        }
        fileLoc = location;
        run();
    }

    public static String getLocation(Element el) {
        return FileWritingTemplate.getBaseDirectoryName() + '/'
                + el.getAttribute("xlink:href");
    }

    public void change(EventAccessOperations event, Status status) {
        OutputScheduler.getDefault().schedule(this);
    }

    public String getLocation() {
        return fileLoc;
    }

    public void run() {
        OpenMap map = pool.getMap(new FreshnessEventColorizer());
        try {
            try {
                EventLayer el = map.getEventLayer();
                el.eventDataChanged(new EQDataEvent(events.getAll()));
                map.writeMapToPNG(fileLoc);
            } catch(SQLException e) {
                GlobalExceptionHandler.handle(e);
            }
        } catch(IOException e) {
            throw new RuntimeException("Trouble writing map", e);
        } finally {
            pool.returnMap(map);
        }
    }

    public void setArmStatus(String status) {}// noImpl

    private static MapPool pool = MapPool.getDefaultPool();

    private JDBCEventStatus events;

    private static Logger logger = Logger.getLogger(MapEventStatus.class);
}