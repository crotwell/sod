package edu.sc.seis.sod.measure;

import java.io.Serializable;


public abstract class Measurement implements Serializable {

    public Measurement(String name) {
        this.name = name;
    }

    public static Measurement createScalar(String name, double value) {
        return new ScalarMeasurement(name, value);
    }

    public abstract String toXMLFragment();

    public String getName() {
        return name;
    }

    String name;
}
