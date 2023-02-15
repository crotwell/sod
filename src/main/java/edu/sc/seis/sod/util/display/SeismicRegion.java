package edu.sc.seis.sod.util.display;

import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.sod.model.event.FlinnEngdahlRegion;
import edu.sc.seis.sod.model.event.FlinnEngdahlType;

public class SeismicRegion extends FlinnEngdahlRegion{
    public SeismicRegion(String name, int num){
        this(name, num, new GeographicRegion[]{});
    }

    public SeismicRegion(String name, int num, GeographicRegion[] children){
        type =  FlinnEngdahlType.SEISMIC_REGION;
        number = num;
        this.name = name;
        for (int i = 0; i < children.length; i++) { kids.add(children[i]); }
    }

    public int getNumber(){ return number; }

    public String getName(){ return name; }

    public void add(GeographicRegion child){ kids.add(child); }

    public GeographicRegion[] getChildren(){
        return (GeographicRegion[])kids.toArray(new GeographicRegion[kids.size()]);
    }

    public String toString(){ return getName(); }

    private List kids = new ArrayList();
    private String name;
}

