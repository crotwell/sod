package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.bag.FlippedChannel;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class IsFlipped implements ChannelSubsetter {

    public StringTree accept(Channel channel, NetworkSource network) throws Exception {
        return new StringTreeLeaf(this, FlippedChannel.check(channel));
    }

}
