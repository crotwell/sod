package edu.sc.seis.sod.subsetter.waveformArm;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.waveformArm.WaveformLogicalSubsetter;

/**
 * eventStationXOR contains a sequence of eventAttrSubsetters. The minimum value of the sequence is 2 and
 * the max value of the sequence is 2.
 *<pre>
 *  &lt;eventStationXOR&gt;
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
 *  &lt;/eventStationXOR&gt;
 *</pre>
 */

public final class EventStationXOR extends  WaveformLogicalSubsetter
    implements EventStationSubsetter {

    public EventStationXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Station station)
        throws Exception {
        EventStationSubsetter filterA = (EventStationSubsetter)filterList.get(0);
        EventStationSubsetter filterB = (EventStationSubsetter)filterList.get(1);
        return ( filterA.accept(event, station) != filterB.accept(event, station));

    }

}// EventStationXOR
