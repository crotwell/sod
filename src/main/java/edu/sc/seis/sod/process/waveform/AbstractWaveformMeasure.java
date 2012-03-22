package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.status.Pass;


public abstract class AbstractWaveformMeasure implements WaveformProcess {

    protected String name;

    public AbstractWaveformMeasure(Element config) {
        name = SodUtil.loadText(config, "name", SodUtil.getSimpleName(getClass()));
    }
    
    @Override
    public WaveformResult accept(CacheEvent event,
                                 ChannelImpl channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws Exception {
        if (seismograms.length != 0) {
            Measurement m = calculate(event, channel, original, available, seismograms, cookieJar);
            cookieJar.put(m.getName(), m);
        }
        return new WaveformResult(seismograms, new Pass(this));
    }
    
    abstract Measurement calculate(CacheEvent event,
                                   ChannelImpl channel,
                                   RequestFilter[] original,
                                   RequestFilter[] available,
                                   LocalSeismogramImpl[] seismograms,
                                   CookieJar cookieJar) throws Exception;

    protected static float[] toFloatArrayAsIfContinuous(LocalSeismogramImpl[] seis) throws FissuresException {
        int npts = 0;
        for (int i = 0; i < seis.length; i++) {
            npts += seis[i].getNumPoints();
        }
        float[] data = new float[npts];
        int pos = 0;
        for (int i = 0; i < seis.length; i++) {
            System.arraycopy(seis[i].get_as_floats(), 0, data, pos, seis[i].getNumPoints());
            pos += seis[i].getNumPoints();
        }
        return data;
    }
}
