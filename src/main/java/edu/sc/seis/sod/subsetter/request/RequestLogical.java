package edu.sc.seis.sod.subsetter.request;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelLogicalSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;
import edu.sc.seis.sod.subsetter.origin.EventLogicalSubsetter;
import edu.sc.seis.sod.subsetter.station.StationLogicalSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public abstract class RequestLogical extends LogicalSubsetter implements RequestSubsetter {

    protected RequestLogical() {}
    
    public RequestLogical(Element config) throws ConfigurationException {
        super(config);
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("request");
        packages.addAll(EventChannelLogicalSubsetter.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }

    @Override
    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public static RequestSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof RequestSubsetter) { return (RequestSubsetter)s; 
        } else { 
            return new RequestSubsetter() {
                EventChannelSubsetter ecs = EventChannelLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event, ChannelImpl channel, RequestFilter[] request, CookieJar cookieJar)
                throws Exception {
                    return ecs.accept(event, channel, cookieJar);
                }
            
            };
        }
    }
    
    
}