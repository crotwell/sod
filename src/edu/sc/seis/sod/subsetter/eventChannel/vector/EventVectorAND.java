/**
 * EventChannelGroupAND.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class EventVectorAND extends EventVectorLogicalSubsetter implements
        EventVectorSubsetter {

    public EventVectorAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree[] result = new StringTree[filterList.size()];
        int i=0;
        while(it.hasNext()) {
            EventVectorSubsetter filter = (EventVectorSubsetter)it.next();
            result[i] = filter.accept(event, channel, cookieJar);
            if(!result[i].isSuccess()) { break; }
            i++;
        }
        return new StringTreeBranch(this, result[i].isSuccess(), result);
    }
}