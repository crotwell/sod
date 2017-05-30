/**
 * Filter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;

public class Filter implements WaveformProcess, Threadable {

    @Deprecated
    public Filter(Element config) throws ConfigurationException {
        throw new ConfigurationException("WARNING: <filter> is deprecated because of excessive memory and cpu usage, please switch to <oregonDSPFilter>");
        
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
        throw new ConfigurationException("WARNING: <filter> is deprecated because of excessive memory and cpu usage, please switch to <oregonDSPFilter>");
        
    }

}
