package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * This subsetter is used to specify a sequence of EventAttrSubsetters. This subsetter is accepted when even one 
 * of the subsetters forming the sequence is accepted. If all the subsetters in the sequence are not accepted then
 * the eventAttrOR is not accepted.
 * &lt;eventAttrOR&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 *            &lt;geographicRegion&gt;&lt;value&gt;162 258 324 404&lt;value/&gt;&lt;&lt;geographicRegion&gt;
 * &lt;/eventAttrOR&gt;
 *
 *                                   (or)
 *
 *  &lt;eventAttrOR&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 * &lt;/eventAttrOR&gt;
 *
 *                                   (or)
 * &lt;eventAttrOR&gt;
 * &lt;/eventAttrOR&gt;
 *
 */

public class EventAttrOR 
    extends EventLogicalSubsetter 
    implements EventAttrSubsetter {
    
    /**
     * Creates a new <code>EventAttrOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventAttrOR (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e an <code>EventAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAttr e,  CookieJar cookies) throws Exception{
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventAttrSubsetter filter = (EventAttrSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return true;
	    }
	}
	return false;
    }

}// EventAttrOR
