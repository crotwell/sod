package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.measure.ListMeasurement;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.measure.QuantityMeasurement;

public class MinMax extends AbstractWaveformMeasure {

    public MinMax(Element el) {
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
        List<Measurement> out = new ArrayList<Measurement>();
        out.add(new QuantityMeasurement("min", new QuantityImpl(stat.min(), seismograms[0].getUnit())));
        out.add(new QuantityMeasurement("max", new QuantityImpl(stat.max(), seismograms[0].getUnit())));
        return new ListMeasurement("minmax", out);
    }
}
