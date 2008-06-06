package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.bag.NegativeSensitivity;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class HasNegativeSensitivity implements ChannelSubsetter {

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return new StringTreeLeaf(this,
                                  NegativeSensitivity.check(network.retrieve_sensitivity(channel.get_id(),
                                                                                         channel.getEffectiveTime().start_time)));
    }
}
