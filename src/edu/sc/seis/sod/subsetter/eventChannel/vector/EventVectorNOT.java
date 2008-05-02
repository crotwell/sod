/**
 * EventChannelGroupNOT.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import java.util.Iterator;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class EventVectorNOT extends EventVectorLogicalSubsetter implements
        EventVectorSubsetter {

    public EventVectorNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        if(it.hasNext()) {
            EventVectorSubsetter filter = (EventVectorSubsetter)it.next();
            StringTree result = filter.accept(event, channel, cookieJar);
            return new StringTreeBranch(this, !result.isSuccess(), new StringTree[] {result});
        }
        return new StringTreeLeaf(this, false, "Empty NOT");
    }
}