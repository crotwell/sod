package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
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


public class OriginTimeRange extends edu.sc.seis.sod.subsetter.TimeRange implements OriginSubsetter {
    public OriginTimeRange (Element config){ super(config); }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        MicroSecondDate actualDate = new MicroSecondDate(origin.origin_time);
        if( getMSTR().intersects(actualDate)) return true;
        else return false;
    }
}// EventTimeRange
