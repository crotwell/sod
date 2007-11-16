package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class Merge implements WaveformProcess, Threadable {

    
    public WaveformResult process(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return new WaveformResult(ReduceTool.merge(seismograms),
                                  new StringTreeLeaf(this,
                                                     true,
                                                     "Contiguous seismograms merged"));
    }

    public boolean isThreadSafe() {
        return true;
    }
}
