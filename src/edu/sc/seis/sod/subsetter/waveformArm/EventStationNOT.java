package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

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
public final class EventStationNOT
    extends  WaveFormLogicalSubsetter
    implements EventStationSubsetter {

    /**
     * Creates a new <code>EventStationNOT</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventStationNOT (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations o, NetworkAccess network, Station station,  CookieJar cookies)
    throws Exception{
    Iterator it = filterList.iterator();
    if (it.hasNext()) {
        EventStationSubsetter filter = (EventStationSubsetter)it.next();
        if ( filter.accept(o, network, station, cookies)) {
        return false;
        }
    }
    return true;
    }

}// EventStationNOT
