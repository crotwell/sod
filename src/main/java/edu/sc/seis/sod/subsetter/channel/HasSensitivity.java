package edu.sc.seis.sod.subsetter.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class HasSensitivity implements ChannelSubsetter {

    public StringTree accept(Channel channel, NetworkSource network) {
        InstrumentSensitivity sens = channel.getResponse() == null ? null : channel.getResponse().getInstrumentSensitivity();
        return new StringTreeLeaf(this, InstrumentSensitivity.isValid(sens));
    }

    private Logger logger = LoggerFactory.getLogger(HasResponse.class);
}
