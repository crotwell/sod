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
 * &lt;eventAttrXOR&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 *            &lt;geographicRegion&gt;&lt;value&gt;162 258 324 404&lt;value/&gt;&lt;&lt;geographicRegion&gt;
 * &lt;/eventAttrXOR&gt;
 *
 *                                   (or)
 *
 *  &lt;eventAttrXOR&gt;
 *            &lt;seismicRegion&gt;&lt;value&gt;10 20 30 40&lt;value/&gt;&lt;&lt;seismicRegion&gt;
 * &lt;/eventAttrXOR&gt;
 *
 *                                   (or)
 * &lt;eventAttrXOR&gt;
 * &lt;/eventAttrXOR&gt;
 *
 */

public class EventAttrXOR 
    extends EventLogicalSubsetter 
    implements EventAttrSubsetter {
    
    /**
     * Creates a new <code>EventAttrXOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventAttrXOR (Element config) throws ConfigurationException {
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
	EventAttrSubsetter first, second;
	boolean firstBoo, secondBoo;
	if (it.hasNext()) {
	    first = (EventAttrSubsetter)it.next();
	    firstBoo = first.accept(e, cookies);
	    if (it.hasNext()) {
		second = (EventAttrSubsetter)it.next();
		secondBoo = second.accept(e, cookies);
		if ((firstBoo && ! secondBoo) || (! firstBoo && secondBoo)) {
		    return true;
		}
	    }
	}
	return false;
    }

}// EventAttrXOR
