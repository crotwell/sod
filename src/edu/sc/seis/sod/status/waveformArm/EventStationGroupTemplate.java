/**
 * EventStationGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.AllTextTemplate;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.StationTemplate;
import edu.sc.seis.sod.status.Template;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Element;

public class EventStationGroupTemplate extends Template implements GenericTemplate{
    public EventStationGroupTemplate(Element el, EventAccessOperations ev) throws ConfigurationException{
        setEvent(ev);
        parse(el);
    }


    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el)  throws ConfigurationException {
        if (tag.equals("station")){ return new EventStationFormatter(el, ev);}
        else if(tag.equals("statusFilter")){
            if(SodUtil.getNestedText(el).equals("SUCCESS")){
                success = true;
            }
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
        Status status = Status.get(Stage.PROCESSOR, Standing.SUCCESS);
        try {
            Station[] stations;
            synchronized(evStatus){
                if(success){
                    stations = evStatus.getOfStatus(status, ev);
                }else{
                    stations = evStatus.getNotOfStatus(status, ev);
                }
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

    public void setEvent(EventAccessOperations ev){ this.ev = ev; }

    public String getResult(){ return getResult(ev); }

    private EventAccessOperations ev;
    private boolean success = false;

    private static JDBCEventChannelStatus evStatus;

    static{
        try {
            evStatus = new JDBCEventChannelStatus();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }

    }
}

