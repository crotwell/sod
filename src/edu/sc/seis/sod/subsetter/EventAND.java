package edu.sc.seis.sod.filter;

import edu.sc.seis.sod.*;
import java.util.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

/**
 * EventSeqFilter.java
 *
 *
 * Created: Thu Dec 13 21:49:59 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventSeqFilter implements EventFilter {
    public EventSeqFilter (){
    }

    public void add(EventFilter filter) {
	filterList.add(filter);
    }

    public boolean accept(EventAccessOperations e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventFilter filter = (EventFilter)it.next();
	    if ( ! filter.accept(e, cookies)) { //changed from event to e
		return false;
	    } // end of if (! filter.accept(event))
	} // end of while (it.hasNext())
	return true;
    }

  

    List filterList = new LinkedList();

}// EventSeqFilter
