package edu.sc.seis.sod.subsetter.eventChannel;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * EventChannelSubsetter.java Created: Thu Dec 13 17:19:47 2001
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public interface EventChannelSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                          ChannelImpl channel,
                          CookieJar cookieJar) throws Exception;
}// EventChannelSubsetter
