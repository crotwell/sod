package edu.sc.seis.sod.status.eventArm;
import edu.sc.seis.sod.subsetter.eventArm.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.database.event.EventCondition;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.Template;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

public class EventGroupTemplate extends Template implements GenericTemplate, EventStatus{
    protected EventGroupTemplate(){sorter = new EventSorter();}

    public EventGroupTemplate(Element config) {
        parse(config);
    }

    public static EventGroupTemplate createDefaultTemplate(){
        EventGroupTemplate egt = new EventGroupTemplate();
        egt.useDefaultConfig();
        return egt;
    }

    public void parse(Element el) {
        if(el.hasChildNodes() == false) useDefaultConfig();
        else super.parse(el);
        if(sorter == null) sorter = new EventSorter();
    }

    protected void useDefaultConfig(){
        templates.add(new EventFormatter());
        sorter = new EventSorter();
    }


    protected Object getTemplate(String tag, Element el) {
        if(el.getNodeName().equals("eventLabel")){
            return new EventFormatter(el);
        }else if(el.getNodeName().equals("sorting")){
            sorter = new EventSorter(el);
            return textTemplate("");
        }
        return super.getTemplate(tag, el);
    }

    public Object textTemplate(final String text){
        return new EventTemplate(){
            public String getResult(EventAccessOperations ev) { return text; }
        };
    }

    public void change(EventAccessOperations event, EventCondition status) {
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
        public MonitoredEvent(EventAccessOperations event, EventCondition status){
            this.event = event;
            this.status = status;
        }

        public EventAccessOperations event;
        public EventCondition status;
    }

    public void setArmStatus(String status) {
        // NO IMPL
    }

    private List events = new ArrayList();

    private Map eventToMonitors = new HashMap();

    private EventSorter sorter;
}
