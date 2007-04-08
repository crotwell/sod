package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.sc.seis.sod.SodElement;

public class LongitudeRange extends RangeSubsetter implements SodElement {

    public LongitudeRange(Element config) {
        super(config);
        min = BoxAreaImpl.sanitize(min);
        max = BoxAreaImpl.sanitize(max);
    }
}
