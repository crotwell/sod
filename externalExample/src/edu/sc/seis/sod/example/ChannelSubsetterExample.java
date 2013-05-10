package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;

public class ChannelSubsetterExample implements ChannelSubsetter {

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return new Fail(this);
    }
}
