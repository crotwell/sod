package edu.sc.seis.sod.status.eventArm;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.EventDB;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.AllTypeTemplate;
import edu.sc.seis.sod.status.EventFormatter;

public class LastEventTemplate extends AllTypeTemplate {

    public LastEventTemplate(Element el) throws ConfigurationException {
        ef = new EventFormatter(el);
        evAcc = new EventDB();
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