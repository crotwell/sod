package edu.sc.seis.sod.subsetter.request.vector;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorLogicalSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;
import edu.sc.seis.sod.subsetter.request.RequestSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class VectorRequestLogical extends LogicalSubsetter {

    public VectorRequestLogical(Element config) throws ConfigurationException {
        super(config);
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("request.vector");
        packages.add("request");
        packages.addAll(EventVectorLogicalSubsetter.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }

    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public static VectorRequestSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof VectorRequestSubsetter) {
            return (VectorRequestSubsetter)s;
        } else if (s instanceof RequestSubsetter) {
            return new ANDRequestWrapper((RequestSubsetter)s);
        } else {
            return new VectorRequestSubsetter() {
                EventVectorSubsetter ecs = EventVectorLogicalSubsetter.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         ChannelGroup channelGroup,
                                         RequestFilter[][] request,
                                         CookieJar cookieJar) throws Exception {
                    return ecs.accept(event, channelGroup, cookieJar);
                }
            };
        }
    }
}