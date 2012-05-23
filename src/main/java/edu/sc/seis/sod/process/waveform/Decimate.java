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
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeBranch;

public class Decimate implements WaveformProcess, Threadable {

    public Decimate(Element config) {
        this.config = config;
        if (SodUtil.getElement(config, ANTIALIAS_NAME) != null) {
            antiAlias = true;
        }
        if (SodUtil.getElement(config, TOSPS_NAME) != null) {
            toSampleRate = Float.parseFloat(SodUtil.loadText(config, TOSPS_NAME, "not used"));
        } else if (SodUtil.getElement(config, TOSPS_NAME) != null) {
            decimate = new edu.sc.seis.fissuresUtil.bag.Decimate(Integer.parseInt(SodUtil.getNestedText(SodUtil.getElement(config,
                                                                                                                           FACTOR_NAME))));
        } else {
            System.err.println("WARNING, naked int in <decimate> is depricated, please use <" + TOSPS_NAME + "> or <"
                    + FACTOR_NAME + ">.");
            decimate = new edu.sc.seis.fissuresUtil.bag.Decimate(Integer.parseInt(SodUtil.getNestedText(config)));
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
            edu.sc.seis.fissuresUtil.bag.Decimate d = decimate;
            if (d == null) {
                d = new edu.sc.seis.fissuresUtil.bag.Decimate((int)Math.ceil(seismograms[0].getSampling()
                        .getFrequency()
                        .getValue(UnitImpl.SECOND)
                        / toSampleRate));
            }
            if (antiAlias) {
                antiAliasFilter = new OregonDSPFilter(SodUtil.getElement(config, ANTIALIAS_NAME),
                                                      PassbandType.LOWPASS,
                                                      ZERO,
                                                      seismograms[0].getSampling()
                                                              .getFrequency()
                                                              .divideBy(d.getFactor()));
                WaveformResult filtered = antiAliasFilter.apply(seismograms);
                if (! filtered.isSuccess()) {
                    return new WaveformResult(seismograms, new StringTreeBranch(this, false, filtered.getReason()));
                }
                filteredSeis = filtered.getSeismograms();
            }
            for (int i = 0; i < out.length; i++) {
                out[i] = d.apply(filteredSeis[i]);
            }
        }
        return new WaveformResult(true, out, this);
    }

    Element config;

    boolean antiAlias = true;

    float toSampleRate;

    OregonDSPFilter antiAliasFilter;

    edu.sc.seis.fissuresUtil.bag.Decimate decimate;

    public static final String TOSPS_NAME = "maxSamplesPerSec";

    public static final String FACTOR_NAME = "byFactor";

    public static final String ANTIALIAS_NAME = "antiAliasFilter";

    private static final QuantityImpl ZERO = new QuantityImpl(0, UnitImpl.HERTZ);
}
