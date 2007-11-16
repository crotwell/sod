/**
 * EventChannelGroupSubsetter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface EventVectorSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception;
}