package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.bag.Calculus;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class Differentiate implements WaveformProcess, Threadable {


    public boolean isThreadSafe() {
        return true;
    }
    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        for(int i = 0; i < seismograms.length; i++) {
            seismograms[i] = Calculus.differentiate(seismograms[i]);
        }
        return new WaveformResult(true, seismograms, this);
    }
}
