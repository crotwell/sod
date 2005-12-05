package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import org.w3c.dom.Element;

public class OriginDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements OriginSubsetter {

    public OriginDepthRange(Element config) throws Exception {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin origin) {
        QuantityImpl actualDepth = (QuantityImpl)origin.my_location.depth;
        return actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth());
    }
}// OriginDepthRange
