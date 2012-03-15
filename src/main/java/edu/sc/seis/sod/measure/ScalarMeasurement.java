package edu.sc.seis.sod.measure;

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
    
    public String toString() {
        return getName() + " " + getValue();
    }
}
