package edu.sc.seis.sod.subsetter.origin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.FlinnEngdahlType;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public abstract class FlinnEngdahlRegion implements OriginSubsetter {

    public FlinnEngdahlRegion(Element config) {
        region = Integer.parseInt(SodUtil.getNestedText(config));
    }

    public StringTree accept(CacheEvent eventAccess,
                          EventAttrImpl eventAttr,
                          OriginImpl preferred_origin) {
        edu.sc.seis.sod.model.event.FlinnEngdahlRegion reg = eventAttr.region;
        return new StringTreeLeaf(this, reg.type.value() == getType().value() && reg.number == region);
    }

    public abstract FlinnEngdahlType getType();

    private int region;

    private static Logger logger = LoggerFactory.getLogger(FlinnEngdahlRegion.class.getName());
}// FlinnEngdahlRegion
