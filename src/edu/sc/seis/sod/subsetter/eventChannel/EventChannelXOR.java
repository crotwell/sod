package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public final class EventChannelXOR extends EventChannelLogicalSubsetter
        implements EventChannelSubsetter {

    public EventChannelXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          CookieJar cookieJar) throws Exception {
        EventChannelSubsetter filterA = (EventChannelSubsetter)filterList.get(0);
        EventChannelSubsetter filterB = (EventChannelSubsetter)filterList.get(1);
        return (filterA.accept(event, channel, cookieJar) != filterB.accept(event,
                                                                            channel,
                                                                            cookieJar));
    }
}// EventChannelXOR
