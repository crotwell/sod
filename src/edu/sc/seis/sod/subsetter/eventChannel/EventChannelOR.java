package edu.sc.seis.sod.subsetter.eventChannel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class EventChannelOR extends EventChannelLogicalSubsetter
        implements EventChannelSubsetter {

    public EventChannelOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations o,
                          Channel channel,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree[] result = new StringTree[filterList.size()];
        int i=0;
        while(it.hasNext()) {
            EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
            result[i] = filter.accept(o, channel, cookieJar);
            if(result[i].isSuccess()) { break; }
            i++;
        }
        return new StringTreeBranch(this, result[i].isSuccess(), result);
    }
}// EventChannelOR
