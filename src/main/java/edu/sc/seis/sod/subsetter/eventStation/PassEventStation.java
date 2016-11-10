package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PassEventStation implements EventStationSubsetter {

    public PassEventStation() {}

    public PassEventStation(Element config) {}

    public StringTree accept(CacheEvent o,
                             StationImpl station,
                          CookieJar cookieJar) {
        return new StringTreeLeaf(this, true);
    }
}