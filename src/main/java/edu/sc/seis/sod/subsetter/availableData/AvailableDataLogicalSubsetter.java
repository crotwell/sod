package edu.sc.seis.sod.subsetter.availableData;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
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
    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public static AvailableDataSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof AvailableDataSubsetter) {
            return (AvailableDataSubsetter)s;
        } else {
            final RequestSubsetter subsetter = (RequestSubsetter)RequestLogical.createSubsetter(s);
            return new AvailableDataSubsetter() {
                public StringTree accept(CacheEvent event,
                                         Channel channel,
                                         RequestFilter[] request,
                                         RequestFilter[] available,
                                         MeasurementStorage cookieJar) throws Exception {
                    return subsetter.accept(event, channel, request, cookieJar);
                }
            };
        }
    }
}