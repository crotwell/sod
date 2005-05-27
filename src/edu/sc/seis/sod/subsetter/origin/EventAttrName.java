package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.SodUtil;

public class EventAttrName implements OriginSubsetter {

    public EventAttrName(Element config) {
        name = SodUtil.getNestedText(config);
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr attr,
                          Origin origin) {
        return name.equals(attr);
    }

    private String name;
}// EventAttrName
