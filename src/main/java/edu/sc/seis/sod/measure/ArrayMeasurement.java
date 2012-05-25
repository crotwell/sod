package edu.sc.seis.sod.measure;


public class ArrayMeasurement extends Measurement {

    public ArrayMeasurement(String name, float[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public String toXMLFragment() {
        String out = "<arrayMeasurement name=\"" + getName() + "\">\n";
        for (int i = 0; i < value.length; i++) {
            out += "<value index=\""+i+"\">\n";
        }
        out += "</arrayMeasurement>";
        return out;
    }
    
    protected float[] value;
}
