package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;


/**
 *<pre>
 * This subsetter specifies the EventTimeRange.
 *	&lt;eventTimeRange&gt;
 *		&lt;timeRange&gt;
 *			&lt;startTime&gt;1999-01-01T00:00:00Z&lt;/startTime&gt;
 *			&lt;endTime&gt;2003-01-01T00:00:00Z&lt;/endTime&gt;
 *		&lt;/timeRange&gt;
 *	&lt;/eventTimeRange&gt;
 *
 *                         (or)
 *
 *	&lt;eventTimeRange&gt;
 *		&lt;timeRange&gt;
 *			&lt;startTime&gt;1999-01-01T00:00:00Z&lt;/startTime&gt;
 *		&lt;/timeRange&gt;
 *	&lt;/eventTimeRange&gt;
 *
 *                         (or)
 *
 *	&lt;eventTimeRange&gt;
 *		&lt;timeRange&gt;
 *		&lt;/timeRange&gt;
 *	&lt;/eventTimeRange&gt;
 *
 *                         (or)
 *
 *	&lt;eventTimeRange&gt;
 *		&lt;timeRange&gt;
 *			&lt;endTime&gt;1999-01-01T00:00:00Z&lt;/endTime&gt;
 *		&lt;/timeRange&gt;
 *	&lt;/eventTimeRange&gt; 
 *</pre>
 *
 */


public class EventTimeRange extends edu.sc.seis.sod.subsetter.TimeRange implements OriginSubsetter {
    /**
     * Creates a new <code>EventTimeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EventTimeRange (Element config){
	
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event, Origin origin, CookieJar cookies) {
	
	MicroSecondDate actualDate = new MicroSecondDate(origin.origin_time);
	MicroSecondDate startDate = new MicroSecondDate(getStartTime());
	MicroSecondDate endDate = new MicroSecondDate(getEndTime());
	if( actualDate.after(startDate) && actualDate.before(endDate)) return true;
	else return false;
    }
    
}// EventTimeRange
