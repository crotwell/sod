package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class EventStationScript extends AbstractScriptSubsetter implements EventStationSubsetter {

    public EventStationScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, StationImpl station, CookieJar cookieJar) throws Exception {
        return runScript(new VelocityEvent(event), new VelocityStation(station), cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityEvent event,  VelocityStation station, CookieJar cookieJar) throws Exception {
        engine.put("station", station);
        engine.put("event", event);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
