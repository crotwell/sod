package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.GenericTemplate;
import edu.sc.seis.sod.subsetter.EventFormatter;
import edu.sc.seis.sod.subsetter.Template;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

public class EventGroupTemplate extends Template implements GenericTemplate, EventStatus{
    
    public EventGroupTemplate(){ this(null); }
    
    public EventGroupTemplate(Element config){
        super(config);
        if(sorter == null) sorter = new EventSorter();
    }
    
    protected void useDefaultConfig(){
        templates.add(new EventFormatter());
    }
    
    
    protected Object getTemplate(String tag, Element el) {
        if(el.getNodeName().equals("eventLabel")){
            gen = new EventFormatter(el);
            return gen;
        }else if(el.getNodeName().equals("sorting")){
            sorter = new EventSorter(el);
            return textTemplate("");
        }
        return null;
    }
    
    public Object textTemplate(final String text){
        return new EventTemplate(){
            public String getResult(EventAccessOperations ev) { return text; }
        };
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
    
    public String getResult(){
        StringBuffer output = new StringBuffer();
        List sorted = sorter.getSortedEvents();
        synchronized(sorted){
            Iterator it = sorted.iterator();
            while(it.hasNext()){
                MonitoredEvent curEv = (MonitoredEvent)eventToMonitors.get(it.next());
                Iterator e = templates.iterator();
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
    
    public void setArmStatus(String status) {
        // NO IMPL
    }
    
    private List events = new ArrayList();
    
    private Map eventToMonitors = new HashMap();
    
    private EventFormatter gen;
    
    private EventSorter sorter;
}
