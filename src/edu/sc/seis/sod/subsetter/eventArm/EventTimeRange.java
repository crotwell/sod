package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * EventTimeRange.java
 *
 *
 * Created: Tue Apr  2 14:42:41 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
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
