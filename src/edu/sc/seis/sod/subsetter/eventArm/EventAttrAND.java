package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * EventAttrAND.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventAttrAND 
    extends EventLogicalSubsetter 
    implements EventAttrSubsetter {
    
    /**
     * Creates a new <code>EventAttrAND</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventAttrAND (Element config) throws ConfigurationException {
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
	    if ( !filter.accept(e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// EventAttrAND
