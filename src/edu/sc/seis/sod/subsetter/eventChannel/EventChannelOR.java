package edu.sc.seis.sod.subsetter.eventChannel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;

public final class EventChannelOR extends EventChannelLogicalSubsetter
        implements EventChannelSubsetter {

    public EventChannelOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations o,
                             Channel channel,
                             CookieJar cookieJar) throws Exception {
        StringTree[] result = new StringTree[filterList.size()];
        for(int i = 0; i < filterList.size(); i++) {
            EventChannelSubsetter f = (EventChannelSubsetter)filterList.get(i);
            result[i] = f.accept(o, channel, cookieJar);
            if(result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, true, result);
            }
        }
        return new StringTreeBranch(this, false, result);
    }
}// EventChannelOR
