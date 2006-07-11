package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Arithmatic;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class Mul implements WaveformProcess {

    public Mul(Element el) {
        val = Float.parseFloat(SodUtil.getText(el));
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return new WaveformResult(true, Arithmatic.mul(seismograms, val), this);
    }

    private float val;
}
