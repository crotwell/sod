package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.ExternalFileTemplate;
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
        if(tag.equals("events") || tag.equals("status")) return true;
        return false;
    }
    
    public Object getInterpreter(String tag, Element el){
        if(tag.equals("events")){
            EventGroupTemplate egt = new EventGroupTemplate(el);
            eventGroups.add(egt);
            return egt;
        }else if(tag.equals("status")) return new StatusFormatter();
        return null;
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        Iterator it = eventGroups.iterator();
        while(it.hasNext()){
            ((EventGroupTemplate)it.next()).change(event, status);
        }
        update();
    }
    
    public void setUp(){ eventGroups = new ArrayList(); }
    
    private class StatusFormatter{
        public String toString(){ return status; }
    }
    
    private List eventGroups;
    
    private String status = "";
}
