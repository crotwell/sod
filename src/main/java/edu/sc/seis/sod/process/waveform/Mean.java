package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.bag.Statistics;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.measure.ScalarMeasurement;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;


public class Mean extends AbstractWaveformMeasure {

    public Mean(Element el) {
        super(el);
    }

    @Override
    Measurement calculate(CacheEvent event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms,
                          MeasurementStorage cookieJar) throws Exception {
        Statistics stat = new Statistics(toFloatArrayAsIfContinuous(seismograms));
        return new ScalarMeasurement(name, stat.mean());
    }
}
