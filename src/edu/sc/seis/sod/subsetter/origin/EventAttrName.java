package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * This tag is used to specify the name of the EventAttr.
 * 
 * <pre>
 * 
 *  &lt;eventAttrName&gt;&lt;value&gt;somename&lt;/value&gt;&lt;/eventAttrName&gt;
 * 
 * </pre>
 */
public class EventAttrName implements OriginSubsetter {

    public EventAttrName(Element config) throws ConfigurationException {
        name = SodUtil.getNestedText(config);
    }

    public boolean accept(EventAccessOperations eventAccess,
                          EventAttr eventAttr,
                          Origin preferred_origin) {
        return name.equals(eventAttr);
    }

    String name;
}// EventAttrName
