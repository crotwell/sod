package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;

public class PassEventChannel implements EventChannelSubsetter,
        EventVectorSubsetter {

    public PassEventChannel() {}

    public PassEventChannel(Element config) {}

    public StringTree accept(CacheEvent o,
                             Channel channel,
                          MeasurementStorage cookieJar) {
        return new StringTreeLeaf(this, true);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channelGroup,
                          MeasurementStorage cookieJar) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}
