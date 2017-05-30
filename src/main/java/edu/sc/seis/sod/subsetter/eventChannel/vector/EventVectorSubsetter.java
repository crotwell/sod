/**
 * EventChannelGroupSubsetter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface EventVectorSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                          ChannelGroup channelGroup,
                          CookieJar cookieJar) throws Exception;
}