package edu.sc.seis.sod.subsetter.eventStation;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public final class EventStationNOT extends EventStationLogicalSubsetter
        implements EventStationSubsetter {

    public EventStationNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        if(it.hasNext()) {
            EventStationSubsetter filter = (EventStationSubsetter)it.next();
            if(filter.accept(o, station, cookieJar)) { return false; }
        }
        return true;
    }
}// EventStationNOT
