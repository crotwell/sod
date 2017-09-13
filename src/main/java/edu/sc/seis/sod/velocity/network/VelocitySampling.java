package edu.sc.seis.sod.velocity.network;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.SamplingImpl;

@Deprecated
public class VelocitySampling extends SamplingImpl {

    public VelocitySampling(SamplingImpl samp) {
        this.interval = samp.interval;
        this.numPoints = samp.numPoints;
    }

    public String getIntervalUnitName() {
        //return getTimeInterval().getUnit().name;
        return "second";
    }

    public double getIntervalValue() {
        return TimeUtils.durationToDoubleSeconds(getTimeInterval());
    }
}
