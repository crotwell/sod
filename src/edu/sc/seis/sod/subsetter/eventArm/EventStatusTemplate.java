package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.ExternalFileTemplate;
import edu.sc.seis.sod.subsetter.GenericTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

public class EventStatusTemplate extends ExternalFileTemplate implements EventStatus{
    public EventStatusTemplate(Element el)throws IOException{ super(el); }
    
    public void setArmStatus(String status) {
        this.status = status;
        update();
    }
    
    public boolean isInterpreted(String tag){
        if(tag.equals("events") || tag.equals("status") || tag.equals("mapImgLink"))
            return true;
        return false;
    }
    
    public Object getInterpreter(String tag, Element el){
        if(tag.equals("events")){
            EventGroupTemplate egt = new EventGroupTemplate(el);
            internalStatusWatchers.add(egt);
            return egt;
        }else if(tag.equals("status")) return new StatusFormatter();
        else if(tag.equals("mapImgLink")){
            MapEventStatus mapStatus = new MapImgSrc(el);
            internalStatusWatchers.add(mapStatus);
            return mapStatus;
        }
        return null;
    }
    
    private class MapImgSrc extends MapEventStatus implements GenericTemplate{
        public MapImgSrc(Element el){ super(el); }
        
        public String getResult() { return fileLoc; }
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        Iterator it = internalStatusWatchers.iterator();
        while(it.hasNext()) ((EventStatus)it.next()).change(event, status);
        update();
    }
    
    public void setUp(){ internalStatusWatchers = new ArrayList(); }
    
    private class StatusFormatter implements GenericTemplate{
        public String getResult(){ return status; }
    }
    
    private List internalStatusWatchers;
    
    private String status = "";
}
