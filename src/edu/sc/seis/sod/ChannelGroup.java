/**
 * ChannelGroup.java
 *
 * @author Jagadeesh Danala
 * @version
 */

package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import org.apache.log4j.Logger;

public class ChannelGroup {
    public ChannelGroup(Channel[] channels) {
        this.channels = channels;
    }

    public Channel[] getChannels() {
        return channels;
    }
    public boolean contains(Channel c) {
        for(int i=0;i<channels.length;i++) {
            if(channels[i].equals(c)) return true;
        }
        // didn't find by object equals, check for ids
        for(int i=0;i<channels.length;i++) {
            logger.warn("Found two channels with same id that are not equals()"+ChannelIdUtil.toString(c.get_id()));
            if(ChannelIdUtil.areEqual(channels[i].get_id(), c.get_id())) return true;
        }
        return false;
    }
    private Channel[] channels;

    private static final Logger logger = Logger.getLogger(ChannelGroup.class);

}


