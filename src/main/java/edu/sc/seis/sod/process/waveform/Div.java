package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.bag.Arithmatic;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class Div implements WaveformProcess, Threadable {

    public Div(Element el) {
        val = Float.parseFloat(SodUtil.getText(el));
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return new WaveformResult(true, Arithmatic.div(seismograms, val), this);
    }

    private float val;
}
