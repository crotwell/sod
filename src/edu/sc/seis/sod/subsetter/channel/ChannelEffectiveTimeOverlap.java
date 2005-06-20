package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
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

    public boolean accept(Channel channel, ProxyNetworkAccess network) {
        return overlaps(channel.effective_time);
    }

    static Category logger = Category.getInstance(ChannelEffectiveTimeOverlap.class.getName());
}// ChannelEffectiveTimeOverlap
