package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public final class EventStationXOR extends EventStationLogicalSubsetter
        implements EventStationSubsetter {

    public EventStationXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        EventStationSubsetter filterA = (EventStationSubsetter)filterList.get(0);
        EventStationSubsetter filterB = (EventStationSubsetter)filterList.get(1);
        return (filterA.accept(event, station, cookieJar) != filterB.accept(event,
                                                                            station,
                                                                            cookieJar));
    }
}// EventStationXOR
