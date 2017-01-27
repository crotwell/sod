package edu.sc.seis.sod.measure;

import edu.iris.Fissures.model.MicroSecondDate;

public class TimeMeasurement extends Measurement {

    public TimeMeasurement(String name, MicroSecondDate value) {
        super(name);
        this.value = value;
    }

    @Override
    public String toXMLFragment() {
        return value.getFissuresTime().date_time;
    }

    @Override
    public Object valueAsJSON() {
        return value.getFissuresTime().date_time;
    }
    
    MicroSecondDate value;
}
