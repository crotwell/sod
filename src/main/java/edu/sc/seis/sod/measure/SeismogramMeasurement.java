package edu.sc.seis.sod.measure;

import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class SeismogramMeasurement extends Measurement {

    public SeismogramMeasurement(String name, LocalSeismogramImpl waveform) {
        super(name);
    }

    public LocalSeismogramImpl getWaveform() {
        return waveform;
    }

    LocalSeismogramImpl waveform;

    @Override
    public String toXMLFragment() {
        throw new RuntimeException("SeismogramMeasurement.toXMLFragment() not yet implemented.");
    }

    @Override
    public Object valueAsJSON() {
        throw new RuntimeException("SeismogramMeasurement.valueAsJSON() not yet implemented.");
    }
    
    
}
