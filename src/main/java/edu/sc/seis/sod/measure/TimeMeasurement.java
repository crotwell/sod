package edu.sc.seis.sod.measure;

import java.time.Instant;

public class TimeMeasurement extends Measurement {

    public TimeMeasurement(String name, Instant value) {
        super(name);
        this.value = value;
    }

    @Override
    public String toXMLFragment() {
        return value.getISOString();
    }

    @Override
    public Object valueAsJSON() {
        return value.getISOString();
    }
    
    Instant value;
}
