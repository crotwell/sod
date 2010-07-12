package edu.sc.seis.sod.subsetter.availableData;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.request.RequestLogical;
import edu.sc.seis.sod.subsetter.request.RequestSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class AvailableDataLogicalSubsetter extends LogicalSubsetter {

    public AvailableDataLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("availableData");
        packages.addAll(RequestLogical.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }


    @Override
    protected Subsetter getSubsetter(final Subsetter s) {
        return createSubsetter(s);
    }
    
    public static AvailableDataSubsetter createSubsetter(final Subsetter s) {
        if (s instanceof AvailableDataSubsetter) {
            return (AvailableDataSubsetter)s;
        } else {
            return new AvailableDataSubsetter() {
                
                public StringTree accept(CacheEvent event,
                                         ChannelImpl channel,
                                         RequestFilter[] request,
                                         RequestFilter[] available,
                                         CookieJar cookieJar) throws Exception {
                    return ((RequestSubsetter)RequestLogical.createSubsetter(s)).accept(event, channel, request, cookieJar);
                }
            };
        }
    }
}