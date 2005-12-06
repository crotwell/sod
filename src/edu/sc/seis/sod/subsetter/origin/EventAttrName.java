package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class EventAttrName implements OriginSubsetter {

    public EventAttrName(Element config) {
        name = SodUtil.getNestedText(config);
    }

    public StringTree accept(EventAccessOperations event,
                          EventAttr attr,
                          Origin origin) {
        return new StringTreeLeaf(this, name.equals(attr));
    }

    private String name;
}// EventAttrName
