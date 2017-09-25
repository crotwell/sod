package edu.sc.seis.sod.subsetter.eventChannel.vector;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelLogicalSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;
import edu.sc.seis.sod.subsetter.eventStation.EventStationLogicalSubsetter;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class EventVectorLogicalSubsetter extends LogicalSubsetter {

    public EventVectorLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("eventChannel.vector");
        packages.add("eventChannel");
        packages.addAll(EventStationLogicalSubsetter.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }

    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public static EventVectorSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof EventVectorSubsetter) {
            return (EventVectorSubsetter)s;
        } else if (s instanceof EventChannelSubsetter || s instanceof ChannelSubsetter) {
            return new ANDEventChannelWrapper(EventChannelLogicalSubsetter.createSubsetter(s));
        } else {
            return new EventVectorSubsetter() {
                EventStationSubsetter ecs = EventStationLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         ChannelGroup channelGroup,
                                         MeasurementStorage cookieJar) throws Exception {
                    return ecs.accept(event, (Station)channelGroup.getStation(), cookieJar);
                }
            };
        }
    }
}