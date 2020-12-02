package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class EventStationScript extends AbstractScriptSubsetter implements EventStationSubsetter {

    public EventStationScript(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    public StringTree accept(CacheEvent event, Station station, MeasurementStorage cookieJar) throws Exception {
        return runScript(new VelocityEvent(event), new VelocityStation(station), cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityEvent event,  VelocityStation station, MeasurementStorage cookieJar) throws Exception {
        engine.put("station", station);
        engine.put("event", event);
        engine.put("cookieJar", cookieJar);
        return eval();
    }
}
