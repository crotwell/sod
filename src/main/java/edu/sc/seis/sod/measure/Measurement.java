package edu.sc.seis.sod.measure;

import java.io.Serializable;

import org.json.JSONObject;


public abstract class Measurement implements Serializable {

    public Measurement(String name) {
        this.name = name;
    }

    public static Measurement createScalar(String name, double value) {
        return new ScalarMeasurement(name, value);
    }

    public abstract String toXMLFragment();
    
    /* returns the value of the measurement as either a String, Double, Boolean, Long, or JSONObject or JSONArray.
     * */
    public abstract Object valueAsJSON();

    public String getName() {
        return name;
    }

    public String getValueJSON() {
        return new JSONObject().put(getName(), getValueJSON()).toString();
    }
    String name;
}
