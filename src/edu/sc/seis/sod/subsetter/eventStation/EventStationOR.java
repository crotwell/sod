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

public final class EventStationOR extends EventStationLogicalSubsetter
        implements EventStationSubsetter {

    public EventStationOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations o,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree result = new StringTreeLeaf(this, false);
        ArrayList resultList = new ArrayList();
        if (! it.hasNext()) {
            return new StringTreeLeaf(this, true, "empty OR");
        }
        while(it.hasNext() && ! result.isSuccess()) {
            EventStationSubsetter filter = (EventStationSubsetter)it.next();
            result = filter.accept(o, station, cookieJar);
            resultList.add(result);
        }
        return new StringTreeBranch(this, result.isSuccess(), (StringTree[])resultList.toArray(new StringTree[0]));
    }
}// EventStationOR
