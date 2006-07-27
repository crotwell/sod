package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.channel.Sampling;

public class SeismogramSampling implements WaveformProcess {

    public SeismogramSampling(Element el) throws ConfigurationException {
        chanSampleSubsetter = new Sampling(el);
    }

    Sampling chanSampleSubsetter;

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
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
