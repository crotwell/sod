package edu.sc.seis.sod.velocity.network;

import edu.sc.seis.sod.model.station.Sensitivity;
import edu.sc.seis.sod.util.display.ThreadSafeDecimalFormat;


public class VelocitySensitivity {
    
    public VelocitySensitivity(Sensitivity wrapped) {
        this.wrapped = wrapped;
    }
    
    public String getFactor() {
        return expFormat.format(wrapped.sensitivity_factor);
    }
    
    public String getFrequency() {
        return freqFormat.format(wrapped.frequency);
    }
    
    public String toString() {
        return getFactor()+" ("+getFrequency()+" Hz)";
    }

    ThreadSafeDecimalFormat expFormat = new ThreadSafeDecimalFormat("0.###E0");
    
    ThreadSafeDecimalFormat freqFormat = new ThreadSafeDecimalFormat("0.000");

    Sensitivity wrapped;
}
