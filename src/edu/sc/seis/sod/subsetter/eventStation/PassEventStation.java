package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.CookieJar;

/**
 * Describe class <code>NullEventStationSubsetter</code> here.
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version 1.0
 */
public class PassEventStation implements EventStationSubsetter {

    public PassEventStation() {}

    public PassEventStation(Element config) {}

    public boolean accept(EventAccessOperations o,
                          Station station,
                          CookieJar cookieJar) {
        return true;
    }
}// NullEventStationSubsetter
