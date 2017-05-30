package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.event.FlinnEngdahlType;

public class SeismicRegion extends FlinnEngdahlRegion {
    public SeismicRegion (Element config){
        super(config);
    }

    public FlinnEngdahlType getType() {
        return FlinnEngdahlType.from_int(0);
    }

}// SeismicRegion
