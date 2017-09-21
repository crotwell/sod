package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.status.StringTreeBranch;

public class RetryAndContinue extends ResultWrapper {

    public RetryAndContinue(Element config) throws ConfigurationException {
        super(config);
    }

    public String toString() {
        return "RetryAndContinue(" + subprocess.toString() + ")";
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
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
