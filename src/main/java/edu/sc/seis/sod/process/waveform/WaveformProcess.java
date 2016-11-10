package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * LocalSeismogramProcess.java Created: Thu Dec 13 18:03:03 2001
 * 
 * @author Philip Crotwell
 */
public interface WaveformProcess extends Subsetter {

    /**
     * Processes localSeismograms, possibly modifying them.
     */
    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception;
}// LocalSeismogramProcessor
