package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelIdUtil;


public class PromiseSeismogramList {

    PromiseSeismogramList(List<RequestFilter> request) {
        this.request = request;
    }

    public List<RequestFilter> getRequest() {
        return request;
    }
    
    public synchronized List<LocalSeismogramImpl> getResult() throws SeismogramSourceException {
        while (matching == null && seismogramSourceException == null) {
            try {
                notifyAll();
                wait();
            } catch(InterruptedException e) {}
        }
        if (seismogramSourceException != null) {
            throw seismogramSourceException;
        }
        return matching;
    }
    
    synchronized void finishRequest(List<LocalSeismogramImpl> seisList) {
        matching = new ArrayList<LocalSeismogramImpl>();
        for (LocalSeismogramImpl seis : seisList) {
            for (RequestFilter rf : request) {
                if (ChannelIdUtil.areEqualExceptForBeginTime(rf.channel_id, seis.getChannelID())) {
                    TimeRange rfRange = new TimeRange(rf);
                    TimeRange seisRange = new TimeRange(seis);
                    if (rfRange.intersects(seisRange)) {
                        matching.add(seis);
                        break;
                    }
                }
            }
        }
        notifyAll();
    }
    
    synchronized void seismogramSourceException(SeismogramSourceException e) {
        seismogramSourceException = e;
        notifyAll();
    }
    SeismogramSourceException seismogramSourceException;
    List<LocalSeismogramImpl> matching;
    List<RequestFilter> request;
    
}
