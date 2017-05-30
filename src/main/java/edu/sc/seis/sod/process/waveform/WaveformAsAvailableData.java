package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
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
                                                 seismograms[i].getEndTime());
        }
        return seisAvailable;
    }

    AvailableDataSubsetter availData;
}
