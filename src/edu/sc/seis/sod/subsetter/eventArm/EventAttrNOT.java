package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
/**
 * This subsetter is used to specify a negation of EventAttrSubsetter. This subsetter is accepted only when the included
 * subsetter is false.
 *<pre>
 * &lt;eventAttrNOT&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 * &lt;/eventAttrNOT&gt;
 *
 *                                   (or)
 *
 * &lt;eventAttrNOT&gt;
 * &lt;/eventAttrNOT&gt;
 *
 *</pre>
 */


public final class EventAttrNOT extends EventLogicalSubsetter
    implements EventAttrSubsetter {

    /**
     * returns true if the eventAttr subsetter enclosed in the eventAttr subsetter is false
     * else returns false. The maximum number of eventAttrSubsetters that can be enclosed
     * in an eventAttrNOTSubsetter is 1.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventAttrNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAttr e) throws Exception{
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            EventAttrSubsetter filter = (EventAttrSubsetter)it.next();
            if ( filter.accept(e)) { return false; }
        }
        return true;
    }
}// EventAttrNOT
