package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * This tag is used to specify the name of the EventAttr.
 *<pre>
 * &lt;eventAttrName&gt;&lt;value&gt;somename&lt;/value&gt;&lt;/eventAttrName&gt;
 *</pre>
 */

public class EventAttrName implements EventAttrSubsetter {
    
    /**
     * Creates a new <code>EventAttrName</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventAttrName (Element config) throws ConfigurationException {
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e an <code>EventAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAttr e,  CookieJar cookies) {

	return true;
    }

}// EventAttrName
