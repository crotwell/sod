package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * EventAttrXOR.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
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
