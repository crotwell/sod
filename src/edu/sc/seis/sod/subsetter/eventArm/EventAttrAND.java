package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 *<pre>
 * This subsetter is used to specify a sequence of EventAttrSubsetters. This subsetter is accepted only when all the
 * subsetters forming the sequence are accepted.
 * &lt;eventAttrAND&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 *            &lt;geographicRegion&gt;&lt;value&gt;162 258 324 404&lt;value/&gt;&lt;&lt;geographicRegion&gt;
 * &lt;/eventAttrAND&gt;
 *
 *                                   (or)
 *
 *  &lt;eventAttrAND&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 * &lt;/eventAttrAND&gt;
 *
 *                                   (or)
 * &lt;eventAttrAND&gt;
 * &lt;/eventAttrAND&gt;
 *
 *</pre>
 */
public final class EventAttrAND extends EventLogicalSubsetter
    implements EventAttrSubsetter {

    public EventAttrAND (Element config) throws ConfigurationException {
        super(config);
    }

    /**
     * returns true if all the eventAttr subsetters enclosed in the eventAttrAND subsetter are true.
     * and returns false if any one of them is false. The range of eventAttrSubsetters that can be
     * enclosed in an eventAttrANDSubsetter is 0 to UNLIMITED.
     */
    public boolean accept(EventAttr e) throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            EventAttrSubsetter filter = (EventAttrSubsetter)it.next();
            if ( !filter.accept(e)) { return false;  }
        }
        return true;
    }
}// EventAttrAND
