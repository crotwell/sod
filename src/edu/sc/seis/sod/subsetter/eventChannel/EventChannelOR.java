package edu.sc.seis.sod.subsetter.eventChannel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public final class EventChannelOR extends EventChannelLogicalSubsetter
        implements EventChannelSubsetter {

    public EventChannelOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o,
                          Channel channel,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
            if(filter.accept(o, channel, cookieJar)) { return true; }
        }
        return false;
    }
}// EventChannelOR
