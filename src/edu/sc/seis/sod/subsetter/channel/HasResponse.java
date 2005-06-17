package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.ChannelIdUtil;

public class HasResponse implements ChannelSubsetter {

    public boolean accept(Channel channel, NetworkAccess network) {
        try {
            network.retrieve_instrumentation(channel.get_id(),
                                             channel.get_id().begin_time);
            return true;
        } catch(ChannelNotFound e) {
            logger.debug("No instrumentation for "
                    + ChannelIdUtil.toString(channel.get_id()));
            return false;
        }
    }

    private Logger logger = Logger.getLogger(HasResponse.class);
}
