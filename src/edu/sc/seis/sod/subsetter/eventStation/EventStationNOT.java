package edu.sc.seis.sod.subsetter.eventStation;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public final class EventStationNOT extends EventStationLogicalSubsetter
    implements EventStationSubsetter {

    public EventStationNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent o,
                             StationImpl station,
                             CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree result;
        if(it.hasNext()) {
            EventStationSubsetter filter = (EventStationSubsetter)it.next();
            result = filter.accept(o, station, cookieJar);
            return new StringTreeBranch(this, ! result.isSuccess(), result);
        }
        return new StringTreeLeaf(this, true, "Empty NOT");

    }
}// EventStationNOT
