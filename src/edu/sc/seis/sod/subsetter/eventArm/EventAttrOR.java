package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * EventAttrOR.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventAttrOR 
    extends LogicalSubsetter 
    implements EventAttrSubsetter {
    
    public EventAttrOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(EventAttr e,  CookieJar cookies) {
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
