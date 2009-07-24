package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RequestCut implements WaveformProcess, Threadable {

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] cutSeis = PhaseCut.cut(seismograms, original);
        return new WaveformResult(cutSeis,
                                  new StringTreeLeaf(this, cutSeis.length != 0));
    }
}
