package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Calculus;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Threadable;

public class Differentiate implements WaveformProcess, Threadable {


    public boolean isThreadSafe() {
        return true;
    }
    public WaveformResult process(CacheEvent event,
                                  Channel channel,
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
