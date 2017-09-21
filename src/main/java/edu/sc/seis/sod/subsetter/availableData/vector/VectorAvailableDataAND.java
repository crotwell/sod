/**
 * AvailableDataGroupAND.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorAvailableDataAND extends VectorAvailableDataLogicalSubsetter
        implements VectorAvailableDataSubsetter {

    public VectorAvailableDataAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             ChannelGroup channel,
                             RequestFilter[][] request,
                             RequestFilter[][] available,
                             MeasurementStorage cookieJar) throws Exception {
        StringTree[] result = new StringTree[filterList.size()];
        for(int i = 0; i < filterList.size(); i++) {
            VectorAvailableDataSubsetter filter = (VectorAvailableDataSubsetter)filterList.get(i);
            result[i] = filter.accept(event,
                                      channel,
                                      request,
                                      available,
                                      cookieJar);
            if(!result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, false, result);
            }
        }
        return new StringTreeBranch(this, true, result);
    }
}