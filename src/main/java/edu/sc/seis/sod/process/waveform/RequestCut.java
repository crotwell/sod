package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RequestCut implements WaveformProcess, Threadable {

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] cutSeis = PhaseCut.cut(seismograms, original);
        return new WaveformResult(cutSeis,
                                  new StringTreeLeaf(this, cutSeis.length != 0));
    }
}
