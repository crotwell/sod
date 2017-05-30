package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.bag.AreaUtil;
import edu.sc.seis.sod.model.common.BoxAreaImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class EventBoxArea implements OriginSubsetter {
    public EventBoxArea(Element el) throws ConfigurationException {
        this.ba = SodUtil.loadBoxArea(el);
    }

    public StringTree accept(CacheEvent eventAccess,
                             EventAttrImpl eventAttr,
                             OriginImpl preferred_origin) {
        return new StringTreeLeaf(this,
                                  AreaUtil.inArea(ba,
                                                  preferred_origin.getLocation()));
    }

    private BoxAreaImpl ba;
}
