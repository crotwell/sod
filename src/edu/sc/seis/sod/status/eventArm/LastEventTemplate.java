package edu.sc.seis.sod.status.eventArm;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.AllTypeTemplate;
import edu.sc.seis.sod.status.EventFormatter;
import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;

public class LastEventTemplate extends AllTypeTemplate{
    public LastEventTemplate(Element el) throws ConfigurationException{
        ef = new EventFormatter(el);
    }

    public String getResult() {
        if(Start.getEventArm() != null){
            return ef.getResult(Start.getEventArm().getLastEvent());
        }else{ return "None"; }
    }

    private EventFormatter ef;
}
