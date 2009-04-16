package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.QuantityImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SiteDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements ChannelSubsetter {

    public SiteDepthRange(Element config) throws Exception {
        super(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        QuantityImpl actualDepth = (QuantityImpl)channel.getSite().getLocation().depth;
        return new StringTreeLeaf(this, actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth()));
    }
}// SiteDepthRange
