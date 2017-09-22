package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.sod.bag.NegativeSensitivity;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class HasNegativeSensitivity implements ChannelSubsetter {

    public StringTree accept(Channel channel, NetworkSource network)
            throws Exception {
        InstrumentSensitivity sense = channel.getResponse() == null ? null : channel.getResponse().getInstrumentSensitivity();
        if (sense == null) {
            return new Fail(this, "InstrumentSensitivity is null");
        }
        return new StringTreeLeaf(this,
                                  NegativeSensitivity.check(sense));
    }
}
