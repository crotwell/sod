package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;


public class WaveformAsAvailableData implements WaveformProcess {

    public WaveformAsAvailableData(AvailableDataSubsetter availData) {
        this.availData = availData;
    }
    
    @Override
    public WaveformResult accept(CacheEvent event,
                                 ChannelImpl channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws Exception {
        RequestFilter[] seisAvailable = toRequestFilter(seismograms);
        return new WaveformResult(seismograms, 
                                  availData.accept(event, channel, original, seisAvailable, cookieJar));
    }
    
    public static RequestFilter[] toRequestFilter(LocalSeismogramImpl[] seismograms) {
        RequestFilter[] seisAvailable = new RequestFilter[seismograms.length];
        for (int i = 0; i < seisAvailable.length; i++) {
            seisAvailable[i] = new RequestFilter(seismograms[i].getChannelID(),
                                                 seismograms[i].begin_time,
                                                 seismograms[i].getEndTime().getFissuresTime());
        }
        return seisAvailable;
    }

    AvailableDataSubsetter availData;
}
