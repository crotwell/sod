package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OriginDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements OriginSubsetter {

    public OriginDepthRange(Element config) throws Exception {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             EventAttr eventAttr,
                             Origin origin) {
        QuantityImpl actualDepth = (QuantityImpl)origin.getLocation().depth;
        if(actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth())) {
            return new StringTreeLeaf(this, true);
        } else {
            return new StringTreeLeaf(this, false);
        }
    }
}// OriginDepthRange
