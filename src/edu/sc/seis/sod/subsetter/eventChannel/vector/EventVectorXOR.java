/**
 * EventChannelGroupXOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class EventVectorXOR extends EventVectorLogicalSubsetter implements
        EventVectorSubsetter {

    public EventVectorXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception {
        EventVectorSubsetter filterA = (EventVectorSubsetter)filterList.get(0);
        StringTree resultA = filterA.accept(event, channel, cookieJar);
        EventVectorSubsetter filterB = (EventVectorSubsetter)filterList.get(1);
        StringTree resultB = filterB.accept(event, channel, cookieJar);
        return new StringTreeBranch(this, resultA.isSuccess() != resultB.isSuccess(), new StringTree[] {resultA, resultB});
    }
}