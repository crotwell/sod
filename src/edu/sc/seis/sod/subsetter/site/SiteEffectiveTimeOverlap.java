package edu.sc.seis.sod.subsetter.site;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;

public class SiteEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        ChannelSubsetter {

    public SiteEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public SiteEffectiveTimeOverlap(TimeRange tr) {
        super(tr);
    }

    static Category logger = Category.getInstance(SiteEffectiveTimeOverlap.class.getName());

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return new StringTreeLeaf(this, overlaps(channel.my_site.effective_time));
    }
}// SiteEffectiveTimeOverlap
