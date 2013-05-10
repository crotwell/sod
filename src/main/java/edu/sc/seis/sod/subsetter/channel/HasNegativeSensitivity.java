package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.bag.NegativeSensitivity;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class HasNegativeSensitivity implements ChannelSubsetter {

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        return new StringTreeLeaf(this,
                                  NegativeSensitivity.check(network.getSensitivity(channel.get_id())));
    }
}
