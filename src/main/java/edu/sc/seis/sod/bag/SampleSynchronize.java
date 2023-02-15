package edu.sc.seis.sod.bag;

import java.time.Duration;

import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class SampleSynchronize {

    public static LocalSeismogramImpl alignTimes(LocalSeismogramImpl main, LocalSeismogramImpl shifty, double maxSamplingDiffRatio) throws FissuresException {

        Duration mainPeriod = main.getSampling().getPeriod();
        Duration shiftyPeriod = shifty.getSampling().getPeriod();
        if (((double)mainPeriod.minus(shiftyPeriod).abs().toNanos()) / shiftyPeriod.toNanos() > maxSamplingDiffRatio) {
            throw new FissuresException("sample periods are not compatible: main="+mainPeriod+"  shifty="+shiftyPeriod);
        }
        Duration misalign = Duration.between(main.getBeginTime(), shifty.getBeginTime());
        Duration moduleSamplePeriod = Duration.ofNanos(misalign.toNanos() % mainPeriod.toNanos());
        LocalSeismogramImpl out = new LocalSeismogramImpl(shifty, shifty.getData());
        out.begin_time = shifty.getBeginTime().minus(moduleSamplePeriod);
        out.sampling_info = main.getSampling();
        return out;
    }
}
