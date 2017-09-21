package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
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
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception;
}// LocalSeismogramProcessor
