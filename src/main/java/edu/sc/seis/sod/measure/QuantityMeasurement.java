package edu.sc.seis.sod.measure;

import edu.iris.Fissures.model.QuantityImpl;


public class QuantityMeasurement extends Measurement {
    

    public QuantityMeasurement(String name, QuantityImpl quantity) {
        super(name);
        this.quantity = quantity;
    }

    @Override
    public String toXMLFragment() {
        return "<scalar><name>" + getName() + "</name><value>" + getQuantity().getValue()+ "</value><unit>" + getQuantity().getUnit() + "</unit></scalar>";
    }
    
    public QuantityImpl getQuantity() {
        return quantity;
    }
    
    QuantityImpl quantity;
}
