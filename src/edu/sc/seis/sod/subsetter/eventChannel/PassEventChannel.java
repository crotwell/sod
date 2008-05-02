package edu.sc.seis.sod.subsetter.eventChannel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;

/**
 * Describe class <code>PassEventChannel</code> here.
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version 1.0
 */
public class PassEventChannel implements EventChannelSubsetter,
        EventVectorSubsetter {

    public PassEventChannel() {}

    public PassEventChannel(Element config) {}

    public StringTree accept(CacheEvent o,
                          Channel channel,
                          CookieJar cookieJar) {
        return new StringTreeLeaf(this, true);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}// NullEventChannelSubsetter
