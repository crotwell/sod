package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.measure.Measurement;
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
                                                     CookieJar cookieJar)
            throws Exception {
        if (seismograms.length != 0) {
            Measurement m = calculate(event, channelGroup, original, available, seismograms, cookieJar);
            cookieJar.put(m.getName(), m);
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
                                   CookieJar cookieJar) throws Exception;
}
