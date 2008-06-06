package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Calculus;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Threadable;

/**
 * @author crotwell Created on Mar 18, 2005
 */
public class Integrate implements WaveformProcess, Threadable {

    public Integrate(Element config) throws ConfigurationException {
        this.config = config;
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        for(int i = 0; i < seismograms.length; i++) {
            seismograms[i] = Calculus.integrate(seismograms[i]);
        }
        return new WaveformResult(true, seismograms, this);
    }

    Element config;
}
