package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorResult;
import edu.sc.seis.sod.status.Pass;

public class WaveformVectorProcess implements
        edu.sc.seis.sod.process.waveform.vector.WaveformVectorProcess {

    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        return new WaveformVectorResult(seismograms, new Pass(this));
    }
}
