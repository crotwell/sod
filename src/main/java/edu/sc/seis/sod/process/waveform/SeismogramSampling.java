package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.channel.Sampling;

public class SeismogramSampling implements WaveformProcess {

    public SeismogramSampling(Element el) throws ConfigurationException {
        chanSampleSubsetter = new Sampling(el);
    }

    Sampling chanSampleSubsetter;

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        for(int i = 0; i < seismograms.length; i++) {
            if(!chanSampleSubsetter.accept(seismograms[i].getSampling())) {
                return new WaveformResult(seismograms,
                                          new StringTreeLeaf(this,
                                                             false,
                                                             " seismogram "
                                                                     + i
                                                                     + " failed:"
                                                                     + seismograms[i].getSampling()));
            }
        }
        return new WaveformResult(true, seismograms, this);
    }
}
