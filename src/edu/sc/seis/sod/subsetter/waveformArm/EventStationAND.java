package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;


/**
 * eventStationAND contains a sequence of eventStationSubsetters. The minimum value of the sequence is 0 and
 * the max value of the sequence is unLimited.
 *<pre>
 *  &lt;eventStationAND&gt;
 *      &lt;phaseExists&gt;
 *          &lt;modelName&gt;prem&lt;/modelName&gt;
 *          &lt;phaseName&gt;ttp&lt;/phaseName&gt;
 *      &lt;/phaseExists&gt;
 *      &lt;phaseInteraction&gt;
 *          &lt;modelName&gt;prem&lt;/modelName&gt;
 *          &lt;phaseName&gt;PcP&lt;/phaseName&gt;
 *          &lt;interactionStyle&gt;PATH&lt;/interactionStyle&gt;
 *          &lt;interactionNumber&gt;1&lt;/interactionNumber&gt;
 *          &lt;relative&gt;
 *              &lt;reference&gt;EVENT&lt;/reference&gt;
 *              &lt;depthRange&gt;
 *                  &lt;unitRange&gt;
 *                      &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *                      &lt;min&gt;-1000&lt;/min&gt;
 *                      &lt;max&gt;1000&lt;/max&gt;
 *                  &lt;/unitRange&gt;
 *              &lt;/depthRange&gt;
 *              &lt;distanceRange&gt;
 *                  &lt;unit&gt;DEGREE&lt;/unit&gt;
 *                  &lt;min&gt;60&lt;/min&gt;
 *                  &lt;max&gt;70&lt;/max&gt;
 *              &lt;/distanceRange&gt;
 *          &lt;/relative&gt;
 *      &lt;/phaseInteraction&gt;
 *  &lt;/eventStationAND&gt;
 *</pre>
 */

public final class EventStationAND  extends  WaveformLogicalSubsetter
    implements EventStationSubsetter {

    public EventStationAND (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o, Station station)
        throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            EventStationSubsetter filter = (EventStationSubsetter)it.next();
            if (!filter.accept(o, station)) {
                return false;
            }
        }
        return true;
    }

}// EventStationAND
