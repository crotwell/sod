package edu.sc.seis.sod.measure;

import org.json.JSONObject;

import edu.sc.seis.sod.model.common.QuantityImpl;



public class QuantityMeasurement extends Measurement {
    

    public QuantityMeasurement(String name, QuantityImpl quantity) {
        super(name);
        this.quantity = quantity;
    }

    @Override
    public String toXMLFragment() {
        return "<scalar name=\"" + getName() + "\"><value>" + getQuantity().getValue()+ "</value><unit>" + getQuantity().getUnit() + "</unit></scalar>";
    }
    
    @Override
    public JSONObject valueAsJSON() {
        JSONObject out = new JSONObject();
        out.append("unit", quantity.getUnit().name);
        out.append("value", quantity.getValue());
        return out;
    }

    public QuantityImpl getQuantity() {
        return quantity;
    }
    
    public String toString() {
        return getQuantity().toString();
    }
    
    QuantityImpl quantity;
}
