package edu.sc.seis.sod.filter;

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

    public boolean accept(EventAccessOperations e) {
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventFilter filter = (EventFilter)it.next();
	    if ( ! filter.accept(event)) {
		return false;
	    } // end of if (! filter.accept(event))
	} // end of while (it.hasNext())
	return true;
    }

    List filterList = new LinkedList();

}// EventSeqFilter
