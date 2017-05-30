package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.measure.ArrayMeasurement;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
;
public class OregonDspFFT extends AbstractWaveformMeasure {

    public OregonDspFFT(Element config) {
        super(config);
    }

    @Override
    Measurement calculate(CacheEvent event,
                          ChannelImpl channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms,
                          CookieJar cookieJar) throws Exception {
        if (seismograms.length != 1) {
            throw new Exception("Require continuous data, but num seismograms="+seismograms.length);
        }
        return new ArrayMeasurement(getName(), forward(seismograms[0].get_as_floats()));
    }

    public static float[] forward(float[] realData) {
        return OregonDspFFT.forward(realData);
    }
    
    /**
     * Performs the inverse fft operation of the realFFT call. 
     */
    public static float[] inverse(float[] realData) {
        return OregonDspFFT.inverse(realData);
    }


    public static float[] correlate(float[] x, float[] y) {
        return OregonDspFFT.correlate(x, y);
    }

    public static float[] convolve(float[] x, float[] y, float delta) {
        return OregonDspFFT.convolve(x, y, delta);
    }


}
