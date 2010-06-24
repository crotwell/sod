package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class EventStationScript extends AbstractScriptSubsetter implements EventStationSubsetter {

    public EventStationScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, StationImpl station, CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("station", station);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
