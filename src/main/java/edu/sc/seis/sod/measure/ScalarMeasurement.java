package edu.sc.seis.sod.measure;

import org.json.JSONObject;

public class ScalarMeasurement extends Measurement {

    public ScalarMeasurement(String name, double value) {
        super(name);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    double value;

    @Override
    public String toXMLFragment() {
        return "<scalar><name>" + getName() + "</name><value>" + getValue() + "</value></scalar>";
    }
    
    @Override
    public Double valueAsJSON() {
        return new Double(this.value);
    }

    public String toString() {
        return getName() + " " + getValue();
    }
}
