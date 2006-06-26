package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.WaveformResult;
import edu.sc.seis.sod.status.Pass;


public class WaveformProcessExample implements WaveformProcess {

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return new WaveformResult(seismograms, new Pass(this));
    }
}
