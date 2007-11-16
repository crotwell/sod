package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;


public class EventStationSubsetterExample implements EventStationSubsetter {

    public StringTree accept(CacheEvent event,
                             Station station,
                             CookieJar cookieJar) throws Exception {
        return new Fail(this);
    }
}
