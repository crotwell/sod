package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeBranch;


public class Upsample implements WaveformProcess {

    public Upsample(Element config) {
        this.config = config;
        if (SodUtil.getElement(config, ANTIALIAS_NAME) != null) {
            antiAlias = true;
        }
        if (SodUtil.getElement(config, TOSPS_NAME) != null) {
            toSampleRate = Float.parseFloat(SodUtil.loadText(config, TOSPS_NAME, "not used"));
        } else if (SodUtil.getElement(config, FACTOR_NAME) != null) {
            upsample = new edu.sc.seis.fissuresUtil.bag.Upsample(Integer.parseInt(SodUtil.getNestedText(SodUtil.getElement(config,
                                                                                                                           FACTOR_NAME))));
        }
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                 ChannelImpl channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        LocalSeismogramImpl[] filteredSeis = seismograms;
        if (seismograms.length != 0) {
            edu.sc.seis.fissuresUtil.bag.Upsample u = upsample;
            if (u == null) {
                u = new edu.sc.seis.fissuresUtil.bag.Upsample((int)Math.floor(seismograms[0].getSampling()
                        .getFrequency()
                        .getValue(UnitImpl.HERTZ)
                        / toSampleRate));
            }
            for (int i = 0; i < out.length; i++) {
                out[i] = u.apply(filteredSeis[i]);
            }
            if (antiAlias) {
                antiAliasFilter = new OregonDSPFilter(SodUtil.getElement(config, ANTIALIAS_NAME),
                                                      PassbandType.LOWPASS,
                                                      ZERO,
                                                      seismograms[0].getSampling()
                                                              .getFrequency()
                                                              .divideBy(2*u.getFactor())); // nyquist is 1/2 sample rate
                WaveformResult filtered = antiAliasFilter.apply(seismograms);
                if (! filtered.isSuccess()) {
                    return new WaveformResult(seismograms, new StringTreeBranch(this, false, filtered.getReason()));
                }
                filteredSeis = filtered.getSeismograms();
            }
        }
        return new WaveformResult(true, out, this);
    }

    Element config;

    boolean antiAlias = true;

    float toSampleRate;

    OregonDSPFilter antiAliasFilter;

    edu.sc.seis.fissuresUtil.bag.Upsample upsample;

    public static final String TOSPS_NAME = "minSamplesPerSec";

    public static final String FACTOR_NAME = "byFactor";

    public static final String ANTIALIAS_NAME = "antiAliasFilter";

    private static final QuantityImpl ZERO = new QuantityImpl(0, UnitImpl.HERTZ);
}
