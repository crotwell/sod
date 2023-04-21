package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.sod.bag.Cut;
import edu.sc.seis.sod.model.common.FissuresException;
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
    
    synchronized void finishRequest(List<LocalSeismogramImpl> seisList) throws SeismogramSourceException {
        try {
        	matching = new ArrayList<LocalSeismogramImpl>();
			for (LocalSeismogramImpl seis : seisList) {
			    for (RequestFilter rf : request) {
			        if (ChannelIdUtil.areEqualExceptForBeginTime(rf.channelId, seis.getChannelID())) {
			            TimeRange rfRange = new TimeRange(rf);
			            TimeRange seisRange = new TimeRange(seis);
			            if (rfRange.intersects(seisRange)) {
			                Cut c = new Cut(rf);
			                LocalSeismogramImpl cutSeis = c.applyEncoded(seis);
			                // temp networks year is often wrong from miniseed, replace with rf
			                cutSeis.channel_id = rf.getChannelId();
			                matching.add(cutSeis);
			                break;
			            }
                    }
			    }
			}
			notifyAll();
		} catch (FissuresException e) {
			throw new SeismogramSourceException(e);
		}
    }
    
    synchronized void seismogramSourceException(SeismogramSourceException e) {
        seismogramSourceException = e;
        notifyAll();
    }
    SeismogramSourceException seismogramSourceException;
    List<LocalSeismogramImpl> matching;
    List<RequestFilter> request;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PromiseSeismogramList.class);
}
