package edu.sc.seis.sod.subsetter.eventChannel;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.channel.ChannelLogicalSubsetter;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.eventStation.EventStationLogicalSubsetter;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;
import edu.sc.seis.sod.subsetter.origin.EventLogicalSubsetter;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class EventChannelLogicalSubsetter extends LogicalSubsetter {

    public EventChannelLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    protected EventChannelLogicalSubsetter() {
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("eventChannel");
        packages.add("eventStation");
        packages.addAll(ChannelLogicalSubsetter.packages);
        packages.addAll(EventLogicalSubsetter.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }
    
    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public static EventChannelSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof EventChannelSubsetter) {
            return (EventChannelSubsetter)s;
        } else if (s instanceof EventStationSubsetter || s instanceof OriginSubsetter) {
            return new EventChannelSubsetter() {
                EventStationSubsetter ecs = EventStationLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         ChannelImpl channel,
                                         CookieJar cookieJar) throws Exception {
                    return ecs.accept(event, (StationImpl)channel.getStation(), cookieJar);
                }
            };
        } else {
            return new EventChannelSubsetter() {
                ChannelSubsetter ecs = ChannelLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         ChannelImpl channel,
                                         CookieJar cookieJar) throws Exception {
                    return ecs.accept(channel, Start.getNetworkArm().getNetworkSource());
                }
            };
        }
    }
}