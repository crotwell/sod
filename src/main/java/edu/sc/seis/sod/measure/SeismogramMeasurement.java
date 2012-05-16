package edu.sc.seis.sod.measure;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

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
        // TODO Auto-generated method stub
        return null;
    }
}
