package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class EventStationAND extends EventStationLogicalSubsetter
        implements EventStationSubsetter {

    public EventStationAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations o,
                             Station station,
                             CookieJar cookieJar) throws Exception {
        StringTree[] result = new StringTree[filterList.size()];
        for(int i = 0; i < filterList.size(); i++) {
            EventStationSubsetter f = (EventStationSubsetter)filterList.get(i);
            result[i] = f.accept(o, station, cookieJar);
            if(!result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, false, result);
            }
        }
        return new StringTreeBranch(this, true, result);
    }
}// EventStationAND
