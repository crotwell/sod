package edu.sc.seis.sod.source.seismogram;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class BatchDataRequest implements SeismogramSourceLocator, Runnable {

    ConstantSeismogramSourceLocator wrappedLocator;
    static int NUM_LOADER_THREADS = 2;
    Thread[] loader = new Thread[NUM_LOADER_THREADS];

    public BatchDataRequest(SeismogramSourceLocator wrappedLocator) throws ConfigurationException {
        if ( ! ( wrappedLocator instanceof ConstantSeismogramSourceLocator)) {
            throw new ConfigurationException("Batch must be for constant source locator like FdsnDataSelect");
        }
        this.wrappedLocator = (ConstantSeismogramSourceLocator)wrappedLocator;
        for (int i = 0; i < loader.length; i++) {
            loader[i] = new Thread(this, "Batch SeismogramSource Loader "+(i+1));
            loader[i].setDaemon(true);
            loader[i].start();
        }
    }
    
    public BatchDataRequest(Element config) throws MalformedURLException, URISyntaxException, ConfigurationException {
        this((SeismogramSourceLocator)SodUtil.load(SodUtil.getFirstEmbeddedElement(config), "seismogram"));
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
                List<PromiseSeismogramList> reqList = popNextBatch();
                List<RequestFilter> rfList = new ArrayList<RequestFilter>();
                for (PromiseSeismogramList batchProxy : reqList) {
                    rfList.addAll(batchProxy.getRequest());
                }
                SeismogramSource sSource = wrappedLocator.getSeismogramSource();
                List<LocalSeismogramImpl> seisList;
                try {
                    seisList = sSource.retrieveData(rfList);
                    for (PromiseSeismogramList batchProxy : reqList) {
                        batchProxy.finishRequest(seisList);
                    }
                } catch(SeismogramSourceException e) {
                    for (PromiseSeismogramList batchProxy : reqList) {
                        batchProxy.seismogramSourceException(e);
                    }
                }
                
            }
        }
    }

    @Override
    public SeismogramSource getSeismogramSource(CacheEvent event,
                                                Channel channel,
                                                RequestFilter[] infilters,
                                                MeasurementStorage cookieJar) throws Exception {
        return new BatchSeismogramSource();
    }

    synchronized PromiseSeismogramList addRequestToBatch(List<RequestFilter> request) {
        PromiseSeismogramList proxy = new PromiseSeismogramList(request);
        nextBatch.add(proxy);
        notifyAll();
        return proxy;
    }

    synchronized List<PromiseSeismogramList> popNextBatch() {
        List<PromiseSeismogramList> out = nextBatch;
        nextBatch = new ArrayList<PromiseSeismogramList>();
        return out;
    }

    List<PromiseSeismogramList> nextBatch = new ArrayList<PromiseSeismogramList>();

    class BatchSeismogramSource implements PromiseSeismogramSource {

        @Override
        public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
            PromiseSeismogramList batch = addRequestToBatch(request);
            return batch.getResult();
        }

        @Override
        public PromiseSeismogramList promiseRetrieveData(List<RequestFilter> request) {
            return promiseRetrieveDataList(Collections.singletonList(request)).get(0);
        }

        @Override
        public List<PromiseSeismogramList> promiseRetrieveDataList(List<List<RequestFilter>> request) {
            List<PromiseSeismogramList> out = new ArrayList<PromiseSeismogramList>();
            for (List<RequestFilter> r : request) {
                out.add(addRequestToBatch(r));
            }
            return out;
        }
    }

}