package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramWaveformWorkUnit;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StringTreeBranch;

public class RetryAndContinue extends ResultWrapper {

    public RetryAndContinue(Element config) throws ConfigurationException {
        super(config);
    }

    public String toString() {
        return "RetryAndContinue(" + subprocess.toString() + ")";
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        if(sodDb == null) {
            sodDb = new SodDB();
        }
        WaveformResult result = subprocess.process(event,
                                                   channel,
                                                   original,
                                                   available,
                                                   seismograms,
                                                   cookieJar);
        if(!result.isSuccess()) {
            sodDb.retry(new LocalSeismogramWaveformWorkUnit(cookieJar.getEventChannelPair()));
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
