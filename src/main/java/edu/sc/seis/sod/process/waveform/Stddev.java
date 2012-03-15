package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.measure.ScalarMeasurement;


public class Stddev extends AbstractWaveformMeasure {

    public Stddev(Element el) {
        super(el);
    }
    
    @Override
    Measurement calculate(CacheEvent event,
                          ChannelImpl channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms,
                          CookieJar cookieJar) throws Exception {
        Statistics stat = new Statistics(toFloatArrayAsIfContinuous(seismograms));
        return new ScalarMeasurement(name, stat.stddev());
    }
}
