package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * This subsetter is used to specify a negation of EventStationSubsetter. This subsetter is accepted only when the included
 * subsetter is false.
 *<pre>
 * &lt;eventStationAND&gt;
 *      &lt;phaseExists&gt;
 *          &lt;modelName&gt;prem&lt;/modelName&gt;
 *          &lt;phaseName&gt;ttp&lt;/phaseName&gt;
 *      &lt;/phaseExists&gt;
 *  &lt;/eventStationAND&gt;
 *</pre>
 */
public final class EventStationNOT extends  WaveformLogicalSubsetter
    implements EventStationSubsetter {

    public EventStationNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o, Station station, CookieJar cookieJar)
        throws Exception{
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            EventStationSubsetter filter = (EventStationSubsetter)it.next();
            if ( filter.accept(o, station, cookieJar)) { return false; }
        }
        return true;
    }
}// EventStationNOT
