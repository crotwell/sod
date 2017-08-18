package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.source.network.NetworkSource;
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

    public StringTree accept(Channel channel, NetworkSource network)
            throws Exception {
        return new StringTreeLeaf(this, overlaps(channel.getSite().getEffectiveTime()));
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SiteEffectiveTimeOverlap.class);
}// SiteEffectiveTimeOverlap
