package edu.sc.seis.sod.subsetter.eventStation;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.origin.EventLogicalSubsetter;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;
import edu.sc.seis.sod.subsetter.station.StationLogicalSubsetter;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class EventStationLogicalSubsetter extends LogicalSubsetter {

    public EventStationLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("eventStation");
        packages.addAll(StationLogicalSubsetter.packages);
        packages.addAll(EventLogicalSubsetter.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }
    
    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }

    public static EventStationSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof EventStationSubsetter) {
            return (EventStationSubsetter)s;
        } else if (s instanceof OriginSubsetter) {
            return new EventStationSubsetter() {
                OriginSubsetter ecs = EventLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         Station station,
                                         MeasurementStorage cookieJar) throws Exception {
                    return ecs.accept(event, (EventAttrImpl)event.get_attributes(), event.getPreferred());
                }
            };
        } else  {
            return new EventStationSubsetter() {
                StationSubsetter ecs = StationLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         Station station,
                                         MeasurementStorage cookieJar) throws Exception {
                    return ecs.accept(station, Start.getNetworkArm().getNetworkSource());
                }
            };
        }
    }
}