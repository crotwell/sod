package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;


/**
 * eventAttrXOR contains a sequence of eventAttrSubsetters. The minimum value of the sequence is 2 and
 * the max value of the sequence is 2.
 *<pre>
 * &lt;eventAttrXOR&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 *            &lt;geographicRegion&gt;&lt;value&gt;162 258 324 404&lt;value/&gt;&lt;&lt;geographicRegion&gt;
 * &lt;/eventAttrXOR&gt;
 *
 *</pre>
 */

public final class EventAttrXOR  extends EventLogicalSubsetter
    implements EventAttrSubsetter {

    public EventAttrXOR (Element config) throws ConfigurationException {
        super(config);
    }

    /**
     * returns true if both of the eventAttr subsetters enclosed in an eventAttrXORSubsetter
     * have the same value. i.e., both of them are true or both of them are false.  The range of
     * eventAttrSubsetters that can be enclosed in an eventAttrXORSubsetter is 2 ... 2
     *
     * @param e an <code>EventAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAttr e) throws Exception{
        EventAttrSubsetter filterA = (EventAttrSubsetter)filterList.get(0);
        EventAttrSubsetter filterB = (EventAttrSubsetter)filterList.get(1);
        return ( filterA.accept(e) != filterB.accept(e));
    }

}// EventAttrXOR
