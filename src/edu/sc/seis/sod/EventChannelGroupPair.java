/**
 * EventChannelGroupPair.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

public class EventChannelGroupPair {

    public EventChannelGroupPair(EventChannelPair[] chanPairs) {
        if (chanPairs == null || chanPairs.length != 3 ) {
            throw new IllegalArgumentException("Number of EventChannelPairs must equal 3");
        }
        this.pairs = chanPairs;
        Channel[] chans = new Channel[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            chans[i] = pairs[i].getChannel();
        }
        this.channels = new ChannelGroup(chans);
    }

    public int getEventDbId() { return pairs[0].getEventDbId(); }

    public Status getStatus(){ return pairs[0].getStatus(); }

    public CacheEvent getEvent(){ return pairs[0].getEvent(); }

    public CookieJar getCookieJar() { return pairs[0].getCookieJar(); }

    public ChannelGroup getChannelGroup() { return channels; }

    public void update(Throwable e, Status status) {
        for (int i = 0; i < pairs.length; i++) {
            pairs[i].update(e, status);
        }
    }

    public void update(Status status){
        for (int i = 0; i < pairs.length; i++) {
            pairs[i].update(status);
        }
    }

    ChannelGroup channels;

    EventChannelPair[] pairs;
}

