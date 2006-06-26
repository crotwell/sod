package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;

public class ChannelSubsetterExample implements ChannelSubsetter {

    public boolean accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return false;
    }
}
