package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;

/**
 * EventAttrAND.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventAttrAND implements EventAttrSubsetter {
    public EventAttrAND (){
	
    }
    
    public void add(EventAttrSubsetter eventSubsetter) {
	filterList.add(eventSubsetter);
    }

    public boolean accept(EventAttr e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventAttrSubsetter filter = (EventAttrSubsetter)it.next();
	    if ( ! filter.accept(e, cookies)) { //changed from event to e
		return false;
	    } // end of if (! filter.accept(event))
	} // end of while (it.hasNext())
	return true;
    }

    List filterList = new LinkedList();

}// EventAttrAND
