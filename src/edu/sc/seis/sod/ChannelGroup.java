/**
 * ChannelGroup.java
 * 
 * @author Jagadeesh Danala
 * @version
 */
package edu.sc.seis.sod;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;

public class ChannelGroup {

    public ChannelGroup(Channel[] channels) {
        this.channels = channels;
        ecps = new EventChannelPair[channels.length];
    }

    public void addEventChannelPair(EventChannelPair ecp) {
        int index = getIndex(ecp.getChannel());
        if(index != -1) {
            ecps[index] = ecp;
        } else {
            throw new IllegalArgumentException("Tried to add event channel pair to channel group with channel not in channel group");
        }
    }

    public EventChannelPair getEventChannelPair(Channel chan) {
        int index = getIndex(chan);
        if(index != -1) {
            return ecps[index];
        } else {
            throw new IllegalArgumentException("Tried to get event channel pair for channel not in channel group");
        }
    }

    public Channel[] getChannels() {
        return channels;
    }

    public boolean contains(Channel c) {
        return getIndex(c) != -1;
    }

    private int getIndex(Channel chan) {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].equals(chan)) return i;
        }
        // didn't find by object equals, check for ids
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqual(channels[i].get_id(), chan.get_id())) {
                logger.warn("Found two channels with same id that are not equals()"
                        + ChannelIdUtil.toString(chan.get_id()));
                return i;
            }
        }
        return -1;
    }

    private Channel[] channels;

    private EventChannelPair[] ecps;

    private static final Logger logger = Logger.getLogger(ChannelGroup.class);
}