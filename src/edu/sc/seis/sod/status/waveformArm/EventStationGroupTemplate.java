/**
 * EventStationGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.AllTextTemplate;
import edu.sc.seis.sod.status.StationTemplate;
import edu.sc.seis.sod.status.Template;
import edu.sc.seis.sod.status.eventArm.EventTemplate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

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

    public String getResult(EventAccessOperations ev) {
        try {
            Station[] stations;
            synchronized(evStatus){
                if(success){ stations = evStatus.getOfStatus(status, ev); }
                else if(failure){ stations = evStatus.getNotOfStatus(status, ev); }
                else{ stations = evStatus.getStations(ev); }
            }
            Iterator it = eventStationFormatters.iterator();
            while(it.hasNext()){
                ((EventStationFormatter)it.next()).setEvent(ev);
            }
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < stations.length; i++) {
                Iterator templateIt = templates.iterator();
                while(templateIt.hasNext()){
                    buf.append(((StationTemplate)templateIt.next()).getResult(stations[i]));
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
    private static final Status status = Status.get(Stage.PROCESSOR,
                                                    Standing.SUCCESS);

    private static JDBCEventChannelStatus evStatus;

    static{
        try {
            evStatus = new JDBCEventChannelStatus();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }

    }
}

