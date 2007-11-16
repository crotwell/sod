package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Arithmatic;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;

public class Div implements WaveformProcess, Threadable {

    public Div(Element el) {
        val = Float.parseFloat(SodUtil.getText(el));
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
        return new WaveformResult(true, Arithmatic.div(seismograms, val), this);
    }

    private float val;
}
