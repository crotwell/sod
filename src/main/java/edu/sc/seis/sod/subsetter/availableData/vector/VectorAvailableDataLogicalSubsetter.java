package edu.sc.seis.sod.subsetter.availableData.vector;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequestLogical;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequestSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class VectorAvailableDataLogicalSubsetter extends LogicalSubsetter {

    public VectorAvailableDataLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("availableData.vector");
        packages.add("availableData");
        packages.addAll(VectorRequestLogical.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }

    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public static VectorAvailableDataSubsetter createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof VectorAvailableDataSubsetter) {
            return (VectorAvailableDataSubsetter)s;
        } else if (s instanceof AvailableDataSubsetter) {
            return new ANDAvailableDataWrapper((AvailableDataSubsetter)s);
        } else {
            return new VectorAvailableDataSubsetter() {
                VectorRequestSubsetter ecs = VectorRequestLogical.createSubsetter(s);
                public StringTree accept(CacheEvent event,
                                         ChannelGroup channelGroup,
                                         RequestFilter[][] original,
                                         RequestFilter[][] available,
                                         MeasurementStorage cookieJar) throws Exception {
                    return ecs.accept(event, channelGroup, original, cookieJar);
                }
            };
        }
    }
}