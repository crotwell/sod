package edu.sc.seis.sod.process.waveform.vector;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.Time;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;


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
        TimeInterval firstSampPeriod = seismograms[0][0].getSampling().getPeriod();
        for (int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl[seismograms[i].length];
            for (int j = 0; j < out[i].length; j++) {
                if (i==0 && j==0) {
                    out[i][j] = seismograms[0][0];
                } else { 
                    TimeInterval sampPeriod = seismograms[i][j].getSampling().getPeriod();
                    if (sampPeriod.subtract(firstSampPeriod).abs().divideBy(firstSampPeriod).getValue() > maxSamplingDiffPercentage) {
                        return new WaveformVectorResult(false, seismograms, "sample periods are not compatible: 0,0="+firstSampPeriod+"  "+i+","+j+"="+sampPeriod);
                    }
                    out[i][j] = alignTimes(seismograms[0][0], seismograms[i][j]);
                }
            }
        }
        return new WaveformVectorResult(true, out, this);
    }
    
    public static LocalSeismogramImpl alignTimes(LocalSeismogramImpl main, LocalSeismogramImpl shifty) throws FissuresException {
        TimeInterval misalign = shifty.getBeginTime().subtract(main.getBeginTime());
        TimeInterval moduleSamplePeriod = new TimeInterval(Math.IEEEremainder(misalign.getValue(shifty.getSampling().getPeriod().getUnit()), 
                                                                              shifty.getSampling().getPeriod().getValue()),
                                                           shifty.getSampling().getPeriod().getUnit());
        LocalSeismogramImpl out = new LocalSeismogramImpl(shifty, shifty.getData());
        out.begin_time = shifty.getBeginTime().subtract(moduleSamplePeriod);
        return out;
    }
    
    protected double maxSamplingDiffPercentage = .01;
}
