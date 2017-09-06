package edu.sc.seis.sod.subsetter.channel;

import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;

public class ChannelEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        ChannelSubsetter {

    public ChannelEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public ChannelEffectiveTimeOverlap(TimeRange tr) {
        super(tr);
    }

    public ChannelEffectiveTimeOverlap(Instant start, Instant end) {
        super(start, end);
    }

    public StringTree accept(Channel channel, NetworkSource network) {
        return new StringTreeLeaf(this, overlaps(new TimeRange(channel)));
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelEffectiveTimeOverlap.class);

}// ChannelEffectiveTimeOverlap
