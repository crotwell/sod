package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public final class EventStationXOR extends EventStationLogicalSubsetter
        implements EventStationSubsetter {

    public EventStationXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             StationImpl station,
                          CookieJar cookieJar) throws Exception {
        EventStationSubsetter filterA = (EventStationSubsetter)filterList.get(0);
        EventStationSubsetter filterB = (EventStationSubsetter)filterList.get(1);
        return new StringTreeLeaf(this, (filterA.accept(event, station, cookieJar) != filterB.accept(event,
                                                                            station,
                                                                            cookieJar)));
    }
}// EventStationXOR
