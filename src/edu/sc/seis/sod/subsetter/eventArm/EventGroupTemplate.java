/**
 * EventGroupTemplate.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.NameGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class EventGroupTemplate{
    public EventGroupTemplate(){ this(null); }
    
    public EventGroupTemplate(Element config){
        if(config != null){
            NodeList children = config.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Element el = (Element)children.item(i);
                if(el.getNodeName().equals("eventLabel")){
                    gen = new NameGenerator(el);
                }else if(el.getNodeName().equals("sorting")){
                    sorter = new EventSorter(el);
                }
            }
        }
        if(gen == null) gen = new NameGenerator();
        if(sorter == null) sorter = new EventSorter();
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
    
    public String toString(){
        if(events.size() == 0) return "No events";
        StringBuffer output = new StringBuffer();
        List sorted = sorter.getSortedEvents();
        synchronized(sorted){
            Iterator it = sorted.iterator();
            while(it.hasNext()){
                MonitoredEvent cur = (MonitoredEvent)eventToMonitors.get(it.next());
                output.append(gen.getName(cur.event));
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
