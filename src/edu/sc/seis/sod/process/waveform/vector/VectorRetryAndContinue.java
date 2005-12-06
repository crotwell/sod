package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.JDBCRetryQueue;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorRetryAndContinue extends VectorResultWrapper {

    public VectorRetryAndContinue(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        if(retryQueue == null) {
            retryQueue = Start.getWaveformArm().getRetryQueue();
        }
        WaveformVectorResult result = subProcess.process(event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         seismograms,
                                                         cookieJar);
        if(!result.isSuccess()) {
            retryQueue.retry(cookieJar.getEventChannelPair().getPairId());
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

    private JDBCRetryQueue retryQueue;
}