package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.bag.FlippedChannel;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class IsFlipped implements ChannelSubsetter {

    public StringTree accept(ChannelImpl channel, NetworkSource network) throws Exception {
        return new StringTreeLeaf(this, FlippedChannel.check(channel));
    }

}
