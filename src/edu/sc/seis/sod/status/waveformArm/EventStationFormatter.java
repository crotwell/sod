/**
 * EventStationFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import java.text.DecimalFormat;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.StationTemplate;

public class EventStationFormatter extends StationFormatter{
    public EventStationFormatter(Element el) throws ConfigurationException{
        super(el);
    }

    public Object getTemplate(String name, Element el){
        if(name.equals("numSuccess")){ return new SuccessfulQuery(); }
        else if(name.equals("numFailed")){ return new FailedQuery(); }
        else if(name.equals("numRetry")){ return new RetryQuery(); }
        else if(name.equals("distance")){ return new Distance(); }
        else if(name.equals("baz")){ return new BackAz(); }
        return super.getTemplate(name, el);
    }

    public void setEvent(CacheEvent ev){ this.ev = ev; }

    private class Distance implements StationTemplate{
        public String getResult(Station station) {
            DistAz dAz = new DistAz(station, ev);
            return df.format(dAz.getDelta());
        }

        private DecimalFormat df = new DecimalFormat("0.00");
    }

    private class BackAz implements StationTemplate{
        public String getResult(Station station) {
            DistAz dAz = new DistAz(station, ev);
            return df.format(dAz.getBaz());
        }

        private DecimalFormat df = new DecimalFormat("0.00");
    }

    private class SuccessfulQuery implements StationTemplate{
        public String getResult(Station station) {
            return "" + evStatus.getNumSuccessful(ev, (StationImpl)station);
        }
    }

    private class FailedQuery implements StationTemplate{
        public String getResult(Station station) {
            return "" + evStatus.getNumFailed(ev, (StationImpl)station);
        }
    }

    private class RetryQuery implements StationTemplate{
        public String getResult(Station station) {
            return "" + evStatus.getNumRetry(ev, (StationImpl)station);
        }
    }
    
    private CacheEvent ev;

    private static SodDB evStatus = new SodDB();;

}


