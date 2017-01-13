package edu.sc.seis.sod.measure;

import org.json.JSONArray;

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
    
    @Override
    public Object valueAsJSON() {
        JSONArray out = new JSONArray(value);
        return out;
    }

    protected float[] value;
}
