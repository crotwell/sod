package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import org.w3c.dom.Element;


/**
 * Describe class <code>NullEventStationSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullEventStationSubsetter implements EventStationSubsetter {
    public NullEventStationSubsetter() {}

    public NullEventStationSubsetter(Element config) {}

    public boolean accept(EventAccessOperations o,  Station station) {
        return true;
    }
}// NullEventStationSubsetter
