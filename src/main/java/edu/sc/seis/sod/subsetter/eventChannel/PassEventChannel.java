package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;

public class PassEventChannel implements EventChannelSubsetter,
        EventVectorSubsetter {

    public PassEventChannel() {}

    public PassEventChannel(Element config) {}

    public StringTree accept(CacheEvent o,
                             ChannelImpl channel,
                          CookieJar cookieJar) {
        return new StringTreeLeaf(this, true);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channelGroup,
                          CookieJar cookieJar) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}
