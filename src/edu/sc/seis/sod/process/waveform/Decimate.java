package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;


public class Decimate implements WaveformProcess, Threadable {
    
    public Decimate(Element config) {
        decimate = new edu.sc.seis.fissuresUtil.bag.Decimate(Integer.parseInt(SodUtil.getNestedText(config)));
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult process(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = decimate.apply(seismograms[i]);
        }
        return new WaveformResult(true, out, this);
    }
    
    edu.sc.seis.fissuresUtil.bag.Decimate decimate;
}
