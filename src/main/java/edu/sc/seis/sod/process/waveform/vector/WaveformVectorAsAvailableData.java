package edu.sc.seis.sod.process.waveform.vector;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.process.waveform.WaveformAsAvailableData;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;


public class WaveformVectorAsAvailableData implements WaveformVectorProcess {

    public WaveformVectorAsAvailableData(VectorAvailableDataSubsetter availData) {
        this.availData = availData;
    }
    
    VectorAvailableDataSubsetter availData;

    @Override
    public WaveformVectorResult accept(CacheEvent event,
                                       ChannelGroup channelGroup,
                                       RequestFilter[][] original,
                                       RequestFilter[][] available,
                                       LocalSeismogramImpl[][] seismograms,
                                       CookieJar cookieJar) throws Exception {

        RequestFilter[][] seisAvailable = new RequestFilter[seismograms.length][];
        for (int i = 0; i < seisAvailable.length; i++) {
            seisAvailable[i] = WaveformAsAvailableData.toRequestFilter(seismograms[i]);
        }
        return new WaveformVectorResult(seismograms, 
                                        availData.accept(event, channelGroup, original, seisAvailable, cookieJar));
    }
}
