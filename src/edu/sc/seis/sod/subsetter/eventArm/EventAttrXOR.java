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
    extends LogicalSubsetter 
    implements EventAttrSubsetter {
    
    public EventAttrXOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(EventAttr e,  CookieJar cookies) {
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
