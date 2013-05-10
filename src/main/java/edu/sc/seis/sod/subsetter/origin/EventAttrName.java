package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class EventAttrName implements OriginSubsetter {

    public EventAttrName(Element config) {
        name = SodUtil.getNestedText(config);
    }

    public StringTree accept(CacheEvent event,
                          EventAttrImpl attr,
                          OriginImpl origin) {
        return new StringTreeLeaf(this, name.equals(attr));
    }

    private String name;
}// EventAttrName
