package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class EventChannelXOR extends EventChannelLogicalSubsetter
        implements EventChannelSubsetter {

    public EventChannelXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             Channel channel,
                          CookieJar cookieJar) throws Exception {
        EventChannelSubsetter filterA = (EventChannelSubsetter)filterList.get(0);
        EventChannelSubsetter filterB = (EventChannelSubsetter)filterList.get(1);
        StringTree resultA = filterA.accept(event, channel, cookieJar);
        StringTree resultB = filterB.accept(event, channel, cookieJar);
        return new StringTreeBranch(this, resultA.isSuccess() != resultB.isSuccess(), new StringTree[] { resultA, resultB});
    }
}// EventChannelXOR
