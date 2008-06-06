package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;

/**
 * LocalSeismogramProcess.java Created: Thu Dec 13 18:03:03 2001
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public interface WaveformProcess {

    /**
     * Processes localSeismograms, possibly modifying them.
     */
    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception;
}// LocalSeismogramProcessor
