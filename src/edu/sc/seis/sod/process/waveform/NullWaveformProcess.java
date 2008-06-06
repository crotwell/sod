package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class NullWaveformProcess implements WaveformProcess {
    public NullWaveformProcess (){    }

    public NullWaveformProcess (Element config){
    }

    public WaveformResult process(CacheEvent event,
                                         ChannelImpl channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar) {
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }


}// NullWaveformProcess
