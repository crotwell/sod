package edu.sc.seis.sod.status.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.event.EventCondition;
import edu.sc.seis.sod.status.PeriodicAction;
import edu.sc.seis.sod.status.eventArm.EventStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import java.io.IOException;

public class MapEventStatus extends PeriodicAction implements SodElement, EventStatus{
    private static List maps = new ArrayList();
    private boolean isDuplicate = false;
    OpenMap map = new OpenMap("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");
    EventLayer events;
    protected String fileLoc;

    public MapEventStatus(Element element){
        this(element, false);
        v = true;
    }

    /**
     * Creates a MapEventStatus that outputs to the location in the attribute
     * xlink:href of the passed in element, and if addToEventArm is true, it
     * adds itself to the EventArm's status listeners
     */
    public MapEventStatus(Element element, boolean addToEventArm){
        fileLoc = element.getAttribute("xlink:href");
        synchronized(maps){
            Iterator it = maps.iterator();
            while(it.hasNext()){
                if (((MapEventStatus)it.next()).fileLoc.equals(fileLoc)){
                    isDuplicate = true;
                    break;
                }
            }
        }
        if (!isDuplicate){
            events = new EventLayer(map.getMapBean());
            map.setEventLayer(events);
        }
        if(addToEventArm)Start.getEventArm().add(this);
    }

    public void change(EventAccessOperations event, EventCondition status){
        if (status == EventCondition.PROCESSOR_PASSED && !isDuplicate){
            events.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{event}));
            actIfPeriodElapsed();
        }
    }

    public String getLocation(){ return fileLoc; }

    public boolean isDuplicateForLocation(){ return isDuplicate; }

    public void act() {
        try {
            map.writeMapToPNG(fileLoc);
        } catch (IOException e) {
            throw new RuntimeException("Trouble writing map", e);
        }
    }

    public void setArmStatus(String status){}// noImpl
    private static Logger logger = Logger.getLogger(MapEventStatus.class);
}


