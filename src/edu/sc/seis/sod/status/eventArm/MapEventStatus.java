package edu.sc.seis.sod.status.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.FreshnessEventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.database.event.StatefulEvent;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.MapPool;
import edu.sc.seis.sod.status.OutputScheduler;
import edu.sc.seis.sod.status.eventArm.EventArmMonitor;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class MapEventStatus implements SodElement, EventArmMonitor, Runnable{
    private String fileLoc;

    public MapEventStatus(Element element){
        this(element, false);
    }

    /**
     * Creates a MapEventStatus that outputs to the location in the attribute
     * xlink:href of the passed in element, and if addToEventArm is true, it
     * adds itself to the EventArm's status listeners
     */
    public MapEventStatus(Element element, boolean addToEventArm){
        try {
            events = new JDBCEventStatus();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("Trouble creating event status db for use in map", e);
        }
        fileLoc = getLocation(element);
        if(addToEventArm){Start.getEventArm().add(this);}
        run();
    }

    public static String getLocation(Element el){
        return FileWritingTemplate.getBaseDirectoryName() + '/' + el.getAttribute("xlink:href");
    }

    public void change(EventAccessOperations event, Status status){
        OutputScheduler.getDefault().schedule(this);
    }

    public String getLocation(){ return fileLoc; }

    public void run() {
        OpenMap map = pool.getMap();
        try {
            try {
                StatefulEvent[] evs = events.getAll();
                EventLayer el = map.getEventLayer();
                for (int i = 0; i < evs.length; i++) {
                    el.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{evs[i]}));
                }
                map.writeMapToPNG(fileLoc);
            } catch (SQLException e) {
                GlobalExceptionHandler.handle(e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Trouble writing map", e);
        }finally{
            pool.returnMap(map);
        }
    }

    public void setArmStatus(String status){}// noImpl

    private static MapPool pool = new MapPool(1, new FreshnessEventColorizer());
    private JDBCEventStatus events;
    private static Logger logger = Logger.getLogger(MapEventStatus.class);
}


