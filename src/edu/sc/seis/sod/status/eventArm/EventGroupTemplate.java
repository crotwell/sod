package edu.sc.seis.sod.status.eventArm;
import java.sql.SQLException;
import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.database.event.StatefulEvent;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.Template;
import edu.sc.seis.sod.subsetter.origin.EventSorter;

public class EventGroupTemplate extends Template implements GenericTemplate{
    protected EventGroupTemplate() {
        sorter = new EventSorter();
    }

    public EventGroupTemplate(Element config)  throws ConfigurationException {
        this();
        parse(config);
    }

    public static EventGroupTemplate createDefaultTemplate(){
        EventGroupTemplate egt = new EventGroupTemplate();
        egt.useDefaultConfig();
        return egt;
    }

    public void parse(Element el) throws ConfigurationException  {
        if(el.hasChildNodes() == false) useDefaultConfig();
        else super.parse(el);
        if(sorter == null) sorter = new EventSorter();
    }

    protected void useDefaultConfig(){
        templates.add(EventFormatter.getDefaultFormatter());
        sorter = new EventSorter();
    }


    protected Object getTemplate(String tag, Element el) throws ConfigurationException {
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

    public String getResult(){
        StringBuffer output = new StringBuffer();
        try {
            StatefulEvent[] events = sorter.getSortedEvents();
            for (int i = 0; i < events.length; i++) {
                StatefulEvent curEv = events[i];
                Iterator e = templates.iterator();
                while(e.hasNext()){
                    Object cur = e.next();
                    if(cur instanceof EventTemplate){
                        output.append(((EventTemplate)cur).getResult(curEv));
                    }else{
                        output.append(cur);
                    }
                }
            }
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("Sorting the events threw this", e);
        }
        return output.substring(0, output.length());//don't include last newline
    }

    public void setArmStatus(String status) {}// NO IMPL
    protected EventSorter sorter;
}
