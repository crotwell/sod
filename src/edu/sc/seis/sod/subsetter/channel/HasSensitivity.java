package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;

public class HasSensitivity implements ChannelSubsetter {

    public boolean accept(Channel channel, ProxyNetworkAccess network) {
        try {
            Sensitivity sens = network.retrieve_sensitivity(channel.get_id(),
                                                            channel.get_id().begin_time);
            return ResponseGain.isValid(sens);
        } catch(ChannelNotFound e) {
            logger.debug("No sensitivity for "
                    + ChannelIdUtil.toString(channel.get_id()));
            return false;
        }
    }

    private Logger logger = Logger.getLogger(HasResponse.class);
}
