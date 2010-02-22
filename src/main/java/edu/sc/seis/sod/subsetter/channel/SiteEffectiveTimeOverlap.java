package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;

public class SiteEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        ChannelSubsetter {

    public SiteEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public SiteEffectiveTimeOverlap(TimeRange tr) {
        super(tr);
    }

    public StringTree accept(ChannelImpl channel, ProxyNetworkAccess network)
            throws Exception {
        return new StringTreeLeaf(this, overlaps(channel.getSite().getEffectiveTime()));
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SiteEffectiveTimeOverlap.class);
}// SiteEffectiveTimeOverlap
