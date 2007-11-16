/**
 * EventStationGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.AllTextTemplate;
import edu.sc.seis.sod.status.StationTemplate;
import edu.sc.seis.sod.status.Template;
import edu.sc.seis.sod.status.eventArm.EventTemplate;

public class EventStationGroupTemplate extends Template implements EventTemplate{
    public EventStationGroupTemplate(Element el) throws ConfigurationException{
        parse(el);
    }


    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el)  throws ConfigurationException {
        if (tag.equals("station")){
            EventStationFormatter esf = new EventStationFormatter(el);
            eventStationFormatters.add(esf);
            return esf;
        }
        else if(tag.equals("statusFilter")){
            if(SodUtil.getNestedText(el).equals("SUCCESS")){ success = true; }
            else if(SodUtil.getNestedText(el).equals("FAILURE")){ failure = true; }
            return new AllTextTemplate("");
        }
        return getCommonTemplate(tag, el);
    }

    /**
     *returns an object of the template type that this class uses, and returns
     * the passed in text when the getResult method of that template type is
     * called
     */
    protected Object textTemplate(final String text) {
        return new StationTemplate(){
            public String getResult(Station sta){ return text; }
        };
    }

    public String getResult(CacheEvent ev) {
        try {
            Iterator staIt ;
                if(success){ staIt = evStatus.getSuccessfulStationsForEvent(ev).iterator(); }
                else if(failure){ staIt = evStatus.getUnsuccessfulStationsForEvent(ev).iterator(); }
                else{ staIt = evStatus.getStationsForEvent(ev).iterator(); }
            Iterator it = eventStationFormatters.iterator();
            while(it.hasNext()){
                ((EventStationFormatter)it.next()).setEvent(ev);
            }
            StringBuffer buf = new StringBuffer();
            while(staIt.hasNext()) {
                Iterator templateIt = templates.iterator();
                while(templateIt.hasNext()){
                    buf.append(((StationTemplate)templateIt.next()).getResult((StationImpl)staIt.next()));
                }
            }
            return buf.toString();
        } catch(Exception e) {
            GlobalExceptionHandler.handle(e);
            return "Event formatting threw an exception.  See the errors page for more details.";
        }
    }

    private List eventStationFormatters = new ArrayList();
    private boolean success = false, failure = false;

    private static SodDB evStatus;

}

