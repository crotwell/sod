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
 * &lt;eventAttrNOT&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 * &lt;/eventAttrNOT&gt;
 *
 *                                   (or)
 *
 * &lt;eventAttrNOT&gt;
 * &lt;/eventAttrNOT&gt;
 *
 */


public class EventAttrNOT 
    extends EventLogicalSubsetter 
    implements EventAttrSubsetter {
    
    /**
     * Creates a new <code>EventAttrNOT</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventAttrNOT (Element config) throws ConfigurationException {
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
	if (it.hasNext()) {
	    EventAttrSubsetter filter = (EventAttrSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// EventAttrNOT
