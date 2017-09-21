/**
 * PassChannelGroupAvailableData.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PassVectorAvailableData implements VectorAvailableDataSubsetter {

    public StringTree accept(CacheEvent event,
                             ChannelGroup channelGroup,
                             RequestFilter[][] request,
                             RequestFilter[][] available,
                             MeasurementStorage cookieJar) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}