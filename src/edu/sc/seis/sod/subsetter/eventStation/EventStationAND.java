package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.util.ArrayList;
import java.util.Iterator;
import org.w3c.dom.Element;

public final class EventStationAND extends EventStationLogicalSubsetter
    implements EventStationSubsetter {

    public EventStationAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations o,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        ArrayList reasons = new ArrayList();
        StringTree result = new StringTreeLeaf(this, true);
        while(it.hasNext() && result.isSuccess()) {
            EventStationSubsetter filter = (EventStationSubsetter)it.next();
            result = filter.accept(o, station, cookieJar);
            reasons.add(result);
        }
        if (reasons.size() < filterList.size()) {
            reasons.add(new StringTreeLeaf("ShortCurcit", result.isSuccess()));
        }
        return new StringTreeBranch("EventStationAND",
                                    result.isSuccess(),
                                        (StringTree[])reasons.toArray(new StringTree[0]));
    }
}// EventStationAND
