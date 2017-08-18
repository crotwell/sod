package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.bag.Statistics;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.measure.ListMeasurement;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.measure.QuantityMeasurement;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class MinMax extends AbstractWaveformMeasure {

    public MinMax(Element el) {
        super(el);
    }

    @Override
    Measurement calculate(CacheEvent event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms,
                          CookieJar cookieJar) throws Exception {
        Statistics stat = new Statistics(toFloatArrayAsIfContinuous(seismograms));
        List<Measurement> out = new ArrayList<Measurement>();
        out.add(new QuantityMeasurement("min", new QuantityImpl(stat.min(), seismograms[0].getUnit())));
        out.add(new QuantityMeasurement("max", new QuantityImpl(stat.max(), seismograms[0].getUnit())));
        return new ListMeasurement("MinMax", out);
    }
}
