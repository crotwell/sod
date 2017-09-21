package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;


public abstract class AbstractWaveformVectorMeasure implements WaveformVectorProcess {
    

    protected String name;

    public AbstractWaveformVectorMeasure(Element config) {
        name = SodUtil.loadText(config, "name", SodUtil.getSimpleName(getClass()));
    }
    
    @Override
    public WaveformVectorResult accept(CacheEvent event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     MeasurementStorage cookieJar)
            throws Exception {
        if (seismograms.length != 0) {
            Measurement m = calculate(event, channelGroup, original, available, seismograms, cookieJar);
            cookieJar.addMeasurement(m.getName(), m.getValueJSON());
            return new WaveformVectorResult(seismograms, new Pass(this));
        }
        return new WaveformVectorResult(seismograms, new Fail(this));
    }
    
    public String getName() {
        return name;
    }
    
    abstract Measurement calculate(CacheEvent event,
                                   ChannelGroup channelGroup,
                                   RequestFilter[][] original,
                                   RequestFilter[][] available,
                                   LocalSeismogramImpl[][] seismograms,
                                   MeasurementStorage cookieJar) throws Exception;
}
