package edu.sc.seis.sod.example;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;

public class EventVectorSubsetterExample implements EventVectorSubsetter {

    public StringTree accept(CacheEvent event,
                             ChannelGroup channel,
                             CookieJar cookieJar) throws Exception {
        return new Pass(this);
    }
}
