package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.bag.FlippedChannel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class IsFlipped implements ChannelSubsetter {

    public StringTree accept(Channel channel, ProxyNetworkAccess network) throws Exception {
        return new StringTreeLeaf(this, FlippedChannel.check(channel));
    }

}
