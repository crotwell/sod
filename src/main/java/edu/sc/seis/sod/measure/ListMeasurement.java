package edu.sc.seis.sod.measure;

import java.util.List;


public class ListMeasurement extends Measurement {

    public ListMeasurement(String name, List<Measurement> list) {
        super(name);
        this.list = list;
    }
    
    
    public List<Measurement> getList() {
        return list;
    }
    
    public Measurement getItem(String name) {
        Measurement out = null;
        for (Measurement item : list) {
            if (item.getName().equals(name)) {
                out = item;
            }
        }
        return out;
    }

    List<Measurement> list;

    @Override
    public String toXMLFragment() {
        String out = "<list name=\""+getName()+"\">\n";
        for (Measurement item : list) {
            out += item.toXMLFragment()+"\n";
        }
        out += "</list>";
        return out;
    }
}
