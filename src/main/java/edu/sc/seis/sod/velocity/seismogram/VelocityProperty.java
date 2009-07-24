package edu.sc.seis.sod.velocity.seismogram;

import edu.iris.Fissures.IfSeismogramDC.Property;

public class VelocityProperty {

    public VelocityProperty(Property prop) {
        this(prop.name, prop.value);
    }

    public VelocityProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    String name;

    String value;
}
