package edu.sc.seis.sod.filter;

import edu.sc.seis.sod.*;
import java.util.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

/**
 * EventAND.java
 *
 *
 * Created: Thu Dec 13 21:49:59 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventAND implements EventSubsetter {
    public EventAND (){
    }

    public void add(EventSubsetter eventSubsetter) {
	filterList.add(eventSubsetter);
    }

    public boolean accept(EventAccessOperations e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventSubsetter filter = (EventSubsetter)it.next();
	    if ( ! filter.accept(e, cookies)) { //changed from event to e
		return false;
	    } // end of if (! filter.accept(event))
	} // end of while (it.hasNext())
	return true;
    }

    List filterList = new LinkedList();

}// EventAND
