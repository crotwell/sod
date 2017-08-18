package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.bag.NegativeSensitivity;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class HasNegativeSensitivity implements ChannelSubsetter {

    public StringTree accept(Channel channel, NetworkSource network)
            throws Exception {
        return new StringTreeLeaf(this,
                                  NegativeSensitivity.check(network.getSensitivity(channel)));
    }
}
