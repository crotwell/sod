package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
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
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception;
}// LocalSeismogramProcessor
