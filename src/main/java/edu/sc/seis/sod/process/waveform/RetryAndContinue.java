package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StringTreeBranch;

public class RetryAndContinue extends ResultWrapper {

    public RetryAndContinue(Element config) throws ConfigurationException {
        super(config);
    }

    public String toString() {
        return "RetryAndContinue(" + subprocess.toString() + ")";
    }

    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        if(sodDb == null) {
            sodDb = SodDB.getSingleton();
        }
        WaveformResult result = subprocess.accept(event,
                                                   channel,
                                                   original,
                                                   available,
                                                   seismograms,
                                                   cookieJar);
        if(!result.isSuccess()) {
            cookieJar.getPair().update(Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                  Standing.RETRY));
            return wrapResult(result);
        }
        return result;
    }

    protected WaveformResult wrapResult(WaveformResult result) {
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       true,
                                                       result.getReason()));
    }

    private SodDB sodDb;
}
