package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class NullWaveformProcess implements WaveformProcess {
    public NullWaveformProcess (){    }

    public NullWaveformProcess (Element config){
    }

    public WaveformResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar) {
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }


}// NullWaveformProcess
