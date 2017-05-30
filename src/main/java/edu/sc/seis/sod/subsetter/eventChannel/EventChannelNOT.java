package edu.sc.seis.sod.subsetter.eventChannel;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public final class EventChannelNOT extends EventChannelLogicalSubsetter
        implements EventChannelSubsetter {

    public EventChannelNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent o,
                             ChannelImpl channel,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        if(it.hasNext()) {
            EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
            StringTree result = filter.accept(o, channel, cookieJar);
            return new StringTreeBranch(this, ! result.isSuccess(), result);
        }
        return new StringTreeLeaf(this, true, "empty NOT");
    }
}
