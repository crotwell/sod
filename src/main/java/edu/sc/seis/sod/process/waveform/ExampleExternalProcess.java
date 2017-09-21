package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class ExampleExternalProcess implements WaveformProcess {

    public ExampleExternalProcess(Element config) throws ConfigurationException {}

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        if(seismograms.length == 1) {
            return new WaveformResult(true, seismograms, this);
        } else {
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf("seismograms does not have length one, "
                                                                 + seismograms.length,
                                                         false));
        }
    }
}