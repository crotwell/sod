package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.NameGenerator;
import edu.sc.seis.sod.subsetter.Template;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

public class EventGroupTemplate extends Template{
    public EventGroupTemplate(){ this(null); }
    
    public EventGroupTemplate(Element config){
        super(config);
        if(sorter == null) sorter = new EventSorter();
    }
    
    protected void useDefaultConfig(){
        pieces.add(new NameGenerator());
    }
    
    
    protected Object getInterpreter(String tag, Element el) {
        if(el.getNodeName().equals("eventLabel")){
            gen = new NameGenerator(el);
            return gen;
        }else if(el.getNodeName().equals("sorting")){
            sorter = new EventSorter(el);
        }
        return new Nothing();
    }
    
    private class Nothing{ public String toString(){ return "";} }
    
    protected boolean isInterpreted(String tag) {
        if(tag.equals("eventLabel") || tag.equals("sorting")) return true;
        return false;
    }
    
    
    public void change(EventAccessOperations event, RunStatus status) {
        if(eventToMonitors.containsKey(event)){
            ((MonitoredEvent)eventToMonitors.get(event)).status = status;
        }else{
            MonitoredEvent e = new MonitoredEvent(event, status);
            eventToMonitors.put(event, e);
            events.add(e);
            sorter.add(event);
        }
    }
    
    public String toString(){ return getResults(); }
    
    public String getResults(){
        StringBuffer output = new StringBuffer();
        List sorted = sorter.getSortedEvents();
        synchronized(sorted){
            Iterator it = sorted.iterator();
            while(it.hasNext()){
                MonitoredEvent curEv = (MonitoredEvent)eventToMonitors.get(it.next());
                Iterator e = pieces.iterator();
                while(e.hasNext()){
                    Object cur = e.next();
                    if(cur instanceof EventTemplate)
                        output.append(((EventTemplate)cur).getResult((curEv.event)));
                    else
                        output.append(cur);
                }
            }
        }
        return output.substring(0, output.length());//don't include last newline
    }
    
    private class MonitoredEvent{
        public MonitoredEvent(EventAccessOperations event, RunStatus status){
            this.event = event;
            this.status = status;
        }
        
        public EventAccessOperations event;
        public RunStatus status;
    }
    
    private List events = new ArrayList();
    
    private Map eventToMonitors = new HashMap();
    
    private NameGenerator gen;
    
    private EventSorter sorter;
}
