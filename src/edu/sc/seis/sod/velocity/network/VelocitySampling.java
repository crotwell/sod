package edu.sc.seis.sod.velocity.network;

import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.model.SamplingImpl;

public class VelocitySampling extends SamplingImpl {

    public VelocitySampling(Sampling samp) {
        this.interval = samp.interval;
        this.numPoints = samp.numPoints;
    }

    public String getIntervalUnitName() {
        return getTimeInterval().the_units.name;
    }

    public double getIntervalValue() {
        return getTimeInterval().value;
    }
}
