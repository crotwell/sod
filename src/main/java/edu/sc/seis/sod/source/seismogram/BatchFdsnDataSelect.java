package edu.sc.seis.sod.source.seismogram;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;

public class BatchFdsnDataSelect implements SeismogramSourceLocator, Runnable {

    FdsnDataSelect fdsnDataSelect;
    Thread loader;

    public BatchFdsnDataSelect(FdsnDataSelect fdsnDataSelect) {
        this.fdsnDataSelect = fdsnDataSelect;
        loader = new Thread(this, "Batch FDSN DataCenter");
        loader.setDaemon(true);
        loader.start();
    }
    
    public BatchFdsnDataSelect(Element config) throws MalformedURLException, URISyntaxException {
        this(new FdsnDataSelect(config));
    }

    public void run() {
        while (!Start.isArmFailure()) {
            if (nextBatch.isEmpty()) {
                try {
                    synchronized(this) {
                        wait();
                    }
                } catch(InterruptedException e) {}
            } else {
                List<BatchProxy> reqList = popNextBatch();
                List<RequestFilter> rfList = new ArrayList<RequestFilter>();
                for (BatchProxy batchProxy : reqList) {
                    rfList.addAll(batchProxy.getRequest());
                }
                // special case of direct fdsn
                SeismogramSource sSource = fdsnDataSelect.getSeismogramSource();
                List<LocalSeismogramImpl> seisList;
                try {
                    seisList = sSource.retrieveData(rfList);
                    for (BatchProxy batchProxy : reqList) {
                        batchProxy.finishRequest(seisList);
                    }
                } catch(SeismogramSourceException e) {
                    for (BatchProxy batchProxy : reqList) {
                        batchProxy.seismogramSourceException(e);
                    }
                }
                
            }
        }
    }

    @Override
    public SeismogramSource getSeismogramSource(CacheEvent event,
                                                ChannelImpl channel,
                                                RequestFilter[] infilters,
                                                CookieJar cookieJar) throws Exception {
        return new BatchSeismogramSource();
    }

    synchronized BatchProxy addRequestToBatch(List<RequestFilter> request) {
        BatchProxy proxy = new BatchProxy(request);
        nextBatch.add(proxy);
        notifyAll();
        return proxy;
    }

    synchronized List<BatchProxy> popNextBatch() {
        List<BatchProxy> out = nextBatch;
        nextBatch = new ArrayList<BatchProxy>();
        return out;
    }

    List<BatchProxy> nextBatch = new ArrayList<BatchProxy>();

    class BatchSeismogramSource implements SeismogramSource {

        @Override
        public List<RequestFilter> availableData(List<RequestFilter> request) throws SeismogramSourceException {
            return request;
        }

        @Override
        public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
            BatchProxy batch = addRequestToBatch(request);
            return batch.getResult();
        }
    }

    class BatchProxy {

        BatchProxy(List<RequestFilter> request) {
            this.request = request;
        }

        public List<RequestFilter> getRequest() {
            return request;
        }
        
        synchronized List<LocalSeismogramImpl> getResult() throws SeismogramSourceException {
            while (matching == null && seismogramSourceException == null) {
                try {
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
}