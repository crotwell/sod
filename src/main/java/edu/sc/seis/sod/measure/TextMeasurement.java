package edu.sc.seis.sod.measure;


public class TextMeasurement extends Measurement {

    public TextMeasurement(String name, String value) {
        super(name);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    String value;

    @Override
    public String toXMLFragment() {
        return "<string><name>" + getName() + "</name><value>" + getValue() + "</value></string>";
    }
    
    @Override
    public String valueAsJSON() {
        return this.value;
    }

    public String toString() {
        return getName() + " " + getValue();
    }
}
