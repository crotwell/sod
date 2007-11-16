package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class EventBoxArea implements OriginSubsetter {
    public EventBoxArea(Element el) throws ConfigurationException {
        this.ba = SodUtil.loadBoxArea(el);
    }

    public StringTree accept(CacheEvent eventAccess,
                             EventAttr eventAttr,
                             Origin preferred_origin) {
        return new StringTreeLeaf(this,
                                  AreaUtil.inArea(ba,
                                                  preferred_origin.my_location));
    }

    private BoxArea ba;
}
