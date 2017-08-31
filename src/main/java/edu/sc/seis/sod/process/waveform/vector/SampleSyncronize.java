package edu.sc.seis.sod.process.waveform.vector;

import java.time.Duration;

import edu.sc.seis.sod.bag.SampleSynchronize;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.util.time.ClockUtil;


public class SampleSyncronize implements WaveformVectorProcess {

    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        if (seismograms.length == 0 || seismograms[0].length == 0) {
            return new WaveformVectorResult(false, seismograms, "no seismograms in first component");
        }
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        Duration firstSampPeriod = seismograms[0][0].getSampling().getPeriod();
        for (int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl[seismograms[i].length];
            for (int j = 0; j < out[i].length; j++) {
                if (i==0 && j==0) {
                    out[i][j] = seismograms[0][0];
                } else { 
                    Duration sampPeriod = seismograms[i][j].getSampling().getPeriod();
                    if (sampPeriod.minus(firstSampPeriod).abs().divideBy(firstSampPeriod).getValue() > maxSamplingDiffPercentage) {
                        return new WaveformVectorResult(false, seismograms, "sample periods are not compatible: 0,0="+firstSampPeriod+"  "+i+","+j+"="+sampPeriod);
                    }
                    out[i][j] = SampleSynchronize.alignTimes(seismograms[0][0], seismograms[i][j], maxSamplingDiffPercentage);
                }
            }
        }
        return new WaveformVectorResult(true, out, this);
    }
    
    
    protected double maxSamplingDiffPercentage = .01f;
}
