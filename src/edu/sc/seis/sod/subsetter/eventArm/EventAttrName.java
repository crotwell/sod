package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;

/**
 * This tag is used to specify the name of the EventAttr.
 *<pre>
 * &lt;eventAttrName&gt;&lt;value&gt;somename&lt;/value&gt;&lt;/eventAttrName&gt;
 *</pre>
 */

public class EventAttrName implements EventAttrSubsetter {
    public EventAttrName (Element config) throws ConfigurationException {}

    public boolean accept(EventAttr e) { return true; }
}// EventAttrName
