package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;

public class PassAvailableData implements AvailableDataSubsetter,
        VectorAvailableDataSubsetter {

    public PassAvailableData() {}

    public PassAvailableData(Element config) {}

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        return new StringTreeLeaf(this, true);
    }

    public StringTree accept(CacheEvent event,
                             ChannelGroup channelGroup,
                             RequestFilter[][] request,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}// PassAvailableData
