package edu.sc.seis.sod.subsetter.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.SodElement;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class MapEventStatus implements SodElement, EventStatus{
    OpenMap map = new OpenMap("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");
    EventLayer events;
    protected String fileLoc;
    
    public MapEventStatus(Element element){
        fileLoc = element.getAttribute("xlink:href");
        events = new EventLayer(map.getMapBean());
        map.setEventLayer(events);
    }
    
    public void change(EventAccessOperations event, RunStatus status){
        if (status == RunStatus.PASSED){
            events.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{event}));
            synchronized(scheduled){
                if(scheduled.equals(Boolean.FALSE)){
                    t.schedule(new MapWriter(), 120*1000);
                    scheduled = new Boolean(true);
                }
            }
            
        }
    }
    
    private class MapWriter extends TimerTask{
        public void run() {
            try {
                map.writeMapToPNG(fileLoc);
            } catch (IOException e) {
                logger.error("unable to save map to file "+fileLoc, e);
            }
            scheduled = new Boolean(false);
        }
    }
    
    private static Timer t = new Timer();
    
    private Boolean scheduled = new Boolean(false);
    
    public void setArmStatus(String status){
        // noImpl
    }
    
    private static Logger logger = Logger.getLogger(MapEventStatus.class);
    
}


