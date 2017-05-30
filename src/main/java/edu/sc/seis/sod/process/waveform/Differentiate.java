package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.bag.Calculus;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;

public class Differentiate implements WaveformProcess, Threadable {


    public boolean isThreadSafe() {
        return true;
    }
    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        for(int i = 0; i < seismograms.length; i++) {
            seismograms[i] = Calculus.differentiate(seismograms[i]);
        }
        return new WaveformResult(true, seismograms, this);
    }
}
