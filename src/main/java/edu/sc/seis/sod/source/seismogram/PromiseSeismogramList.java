package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;


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
                    MicroSecondTimeRange rfRange = new MicroSecondTimeRange(rf);
                    MicroSecondTimeRange seisRange = new MicroSecondTimeRange(seis);
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
