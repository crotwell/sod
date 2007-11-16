package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;


public class EventChannelSubsetterExample implements EventChannelSubsetter {

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             CookieJar cookieJar) throws Exception {
        return new Pass(this);
    }
}
