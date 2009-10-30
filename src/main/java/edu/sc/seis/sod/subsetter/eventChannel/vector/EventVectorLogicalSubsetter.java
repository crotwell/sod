package edu.sc.seis.sod.subsetter.eventChannel.vector;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
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
        } else {
            return new EventVectorSubsetter() {
                EventStationSubsetter ecs = EventStationLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         ChannelGroup channel,
                                         CookieJar cookieJar) throws Exception {
                    return ecs.accept(event, (StationImpl)channel.getStation(), cookieJar);
                }
            };
        }
    }
}