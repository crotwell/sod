package edu.sc.seis.sod.status.eventArm;

import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.EventDB;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.AllTypeTemplate;
import edu.sc.seis.sod.status.EventFormatter;

public class LastEventTemplate extends AllTypeTemplate {

    public LastEventTemplate(Element el) throws ConfigurationException {
        ef = new EventFormatter(el);
        evAcc =  EventDB.getSingleton();
    }

    public String getResult() {
        try {
            CacheEvent ev = evAcc.getLastEvent();
            return ef.getResult(ev);
        } catch(NotFound e) {
            return "None";
        }
    }

    private EventDB evAcc;

    private EventFormatter ef;
}