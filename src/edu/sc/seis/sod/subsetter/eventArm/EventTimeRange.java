package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import org.w3c.dom.Element;


/**
 *<pre>
 * This subsetter specifies the EventTimeRange.
 *  &lt;eventTimeRange&gt;
 *      &lt;timeRange&gt;
 *          &lt;startTime&gt;1999-01-01T00:00:00Z&lt;/startTime&gt;
 *          &lt;endTime&gt;2003-01-01T00:00:00Z&lt;/endTime&gt;
 *      &lt;/timeRange&gt;
 *  &lt;/eventTimeRange&gt;
 *
 *                         (or)
 *
 *  &lt;eventTimeRange&gt;
 *      &lt;timeRange&gt;
 *          &lt;startTime&gt;1999-01-01T00:00:00Z&lt;/startTime&gt;
 *      &lt;/timeRange&gt;
 *  &lt;/eventTimeRange&gt;
 *
 *                         (or)
 *
 *  &lt;eventTimeRange&gt;
 *      &lt;timeRange&gt;
 *      &lt;/timeRange&gt;
 *  &lt;/eventTimeRange&gt;
 *
 *                         (or)
 *
 *  &lt;eventTimeRange&gt;
 *      &lt;timeRange&gt;
 *          &lt;endTime&gt;1999-01-01T00:00:00Z&lt;/endTime&gt;
 *      &lt;/timeRange&gt;
 *  &lt;/eventTimeRange&gt;
 *</pre>
 *
 */


public class EventTimeRange extends edu.sc.seis.sod.subsetter.TimeRange implements OriginSubsetter {
    public EventTimeRange (Element config){ super(config); }

    public boolean accept(EventAccessOperations event, Origin origin) {
        MicroSecondDate actualDate = new MicroSecondDate(origin.origin_time);
        MicroSecondDate startDate = new MicroSecondDate(getStartTime());
        MicroSecondDate endDate = new MicroSecondDate(getEndTime());
        if( actualDate.after(startDate) && actualDate.before(endDate)) return true;
        else return false;
    }
}// EventTimeRange
