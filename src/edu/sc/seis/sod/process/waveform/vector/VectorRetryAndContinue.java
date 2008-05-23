package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorRetryAndContinue extends VectorResultWrapper {

    public VectorRetryAndContinue(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult process(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        if(sodDb == null) {
            sodDb = new SodDB();
        }
        WaveformVectorResult result = subProcess.process(event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         seismograms,
                                                         cookieJar);
        if(!result.isSuccess()) {
            cookieJar.getPair().update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                  Standing.RETRY));
            return wrap(result);
        }
        return result;
    }

    protected WaveformVectorResult wrap(WaveformVectorResult result) {
        return new WaveformVectorResult(result.getSeismograms(),
                                        new StringTreeBranch(this,
                                                             true,
                                                             result.getReason()));
    }

    private SodDB sodDb;
}
