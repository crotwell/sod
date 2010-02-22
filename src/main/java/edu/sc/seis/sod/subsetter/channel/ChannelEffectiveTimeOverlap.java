package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
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

    public ChannelEffectiveTimeOverlap(MicroSecondDate start, MicroSecondDate end) {
        super(start, end);
    }

    public StringTree accept(ChannelImpl channel, ProxyNetworkAccess network) {
        return new StringTreeLeaf(this, overlaps(channel.getEffectiveTime()));
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ChannelEffectiveTimeOverlap.class);

}// ChannelEffectiveTimeOverlap
