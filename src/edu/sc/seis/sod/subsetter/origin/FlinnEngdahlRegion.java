package edu.sc.seis.sod.subsetter.origin;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author <a href="mailto:">Philip Crotwell </a>
 */
public abstract class FlinnEngdahlRegion implements OriginSubsetter {

    public FlinnEngdahlRegion(Element config) {
        region = Integer.parseInt(SodUtil.getNestedText(config));
    }

    public StringTree accept(CacheEvent eventAccess,
                          EventAttr eventAttr,
                          Origin preferred_origin) {
        edu.iris.Fissures.FlinnEngdahlRegion reg = eventAttr.region;
        return new StringTreeLeaf(this, reg.type.value() == getType().value() && reg.number == region);
    }

    public abstract FlinnEngdahlType getType();

    private int region;

    static Category logger = Category.getInstance(FlinnEngdahlRegion.class.getName());
}// FlinnEngdahlRegion
