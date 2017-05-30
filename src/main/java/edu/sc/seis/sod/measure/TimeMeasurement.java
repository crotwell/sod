package edu.sc.seis.sod.measure;

import edu.sc.seis.sod.model.common.MicroSecondDate;

public class TimeMeasurement extends Measurement {

    public TimeMeasurement(String name, MicroSecondDate value) {
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
    
    MicroSecondDate value;
}
