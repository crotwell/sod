package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class NullWaveformProcess implements WaveformProcess {
    public NullWaveformProcess (){    }

    public NullWaveformProcess (Element config){
    }

    public WaveformResult accept(CacheEvent event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar) {
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }


}// NullWaveformProcess
