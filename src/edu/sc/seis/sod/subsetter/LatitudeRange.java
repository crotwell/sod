package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;
import edu.sc.seis.sod.SodElement;

public class LatitudeRange extends RangeSubsetter implements SodElement {

    public LatitudeRange(Element config) {
        super(config);
    }
}
