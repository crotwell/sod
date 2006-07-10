package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.bag.NegativeSensitivity;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;

public class HasNegativeSensitivity implements ChannelSubsetter {

    public boolean accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return NegativeSensitivity.check(network.retrieve_sensitivity(channel.get_id(),
                                                                      channel.effective_time.start_time));
    }
}
