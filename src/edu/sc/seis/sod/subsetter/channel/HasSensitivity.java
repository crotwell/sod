package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;

public class HasSensitivity implements ChannelSubsetter {

    public boolean accept(Channel channel, NetworkAccess network) {
        try {
            Instrumentation instrumentation = network.retrieve_instrumentation(channel.get_id(),
                                                                               channel.get_id().begin_time);
            return ResponseGain.isValid(instrumentation.the_response.the_sensitivity);
        } catch(ChannelNotFound e) {
            logger.debug("No instrumentation for "
                    + ChannelIdUtil.toString(channel.get_id()));
            return false;
        }
    }

    private Logger logger = Logger.getLogger(HasResponse.class);
}
