package edu.sc.seis.sod.subsetter.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.subsetter.GenericTemplate;
import org.w3c.dom.Element;

public class MapEventStatus implements SodElement, EventStatus{
    OpenMap map = new OpenMap("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");
    EventLayer events;
    protected String fileLoc;
    
    public MapEventStatus(Element element){
        fileLoc = element.getAttribute("xlink:link");
        events = new EventLayer(map.getMapBean());
        map.setEventLayer(events);
    }
    
    public void change(EventAccessOperations event, RunStatus status){
        if (status == RunStatus.PASSED){
            events.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{event}));
            map.writeMapToPNG(fileLoc);
        }
    }
    
    public void setArmStatus(String status){
        // noImpl
    }
}


