package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.bag.FlippedChannel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;

public class IsFlipped implements ChannelSubsetter {

    public boolean accept(Channel channel, ProxyNetworkAccess network) throws Exception {
        return FlippedChannel.check(channel);
    }

}
