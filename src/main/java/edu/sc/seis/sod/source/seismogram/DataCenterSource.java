package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;

public class DataCenterSource implements SeismogramSource {

    public DataCenterSource(ProxySeismogramDC seisDC) {
        this.seisDC = seisDC;
    }

    @Override
    public List<RequestFilter> availableData(List<RequestFilter> request) {
        return toList(seisDC.available_data(toArray(request)));
    }

    @Override
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
        try {
            RequestFilter[] infilters = toArray(request);
        LocalSeismogram[] localSeismograms = new LocalSeismogram[0];
        logger.debug("before retrieve_seismograms");
        NSSeismogramDC nsDC = (NSSeismogramDC)seisDC.getWrappedDC(NSSeismogramDC.class);
        if (nsDC.getServerDNS().equals("edu/iris/dmc") && nsDC.getServerName().equals("IRIS_ArchiveDataCenter")) {
            // Archive doesn't support retrieve_seismograms
            // so try using the queue set of retrieve calls
            String id = seisDC.queue_seismograms(infilters);
            logger.info("request id: " + id);
            String status = seisDC.request_status(id);
            int i = 0;
            while (status.equals(RETRIEVING_DATA) && i < 60) {
                logger.info("Waiting for data to be returned from the archive.  We've been waiting for " + i++
                        + " minutes");
                try {
                    Thread.sleep(60 * 1000);
                } catch(InterruptedException ex) {}
                status = seisDC.request_status(id);
            }
            if (status.equals(DATA_RETRIEVED)) {
                localSeismograms = seisDC.retrieve_queue(id);
            } else if (status.equals(RETRIEVING_DATA)) {
                seisDC.cancel_request(id);
                throw new SeismogramSourceException("Looks like the archive lost request ID " + id
                                                    + ".  No data was returned after " + i + " minutes. ");
            }
        } else {
            localSeismograms = seisDC.retrieve_seismograms(infilters);
        }
        logger.debug("after successful retrieve_seismograms");
        if (localSeismograms.length > 0
                && !ChannelIdUtil.areEqual(localSeismograms[0].channel_id, infilters[0].channel_id)) {
            // must be server error
            logger.warn("X Channel id in returned seismogram doesn not match channelid in request. req="
                    + ChannelIdUtil.toString(infilters[0].channel_id) + " seis="
                    + ChannelIdUtil.toString(localSeismograms[0].channel_id));
        }
        List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
        LocalSeismogram[] fromServer = seisDC.retrieve_seismograms(request.toArray(new RequestFilter[0]));
        for (int i = 0; i < fromServer.length; i++) {
            out.add((LocalSeismogramImpl)fromServer[i]);
        }
        return out;
        } catch(FissuresException e) {
            throw new SeismogramSourceException(e);
        }
    }

    public ProxySeismogramDC getDataCenter() {
        return seisDC;
    }

    public static List<RequestFilter> toList(RequestFilter[] in) {
        List<RequestFilter> out = new ArrayList<RequestFilter>();
        for (int i = 0; i < in.length; i++) {
            out.add(in[i]);
        }
        return out;
    }

    public static RequestFilter[] toArray(List<RequestFilter> in) {
        return in.toArray(new RequestFilter[0]);
    }

    public static LocalSeismogramImpl[] toSeisArray(List<LocalSeismogramImpl> data) {
        return data.toArray(new LocalSeismogramImpl[0]);
    }

    ProxySeismogramDC seisDC;

    public static final String RETRIEVING_DATA = "Processing";

    public static final String DATA_RETRIEVED = "Finished";

    public static final String NO_DATA = "no_data";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataCenterSource.class);
}
