package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SiteDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements ChannelSubsetter {

    public SiteDepthRange(Element config) throws Exception {
        super(config);
    }

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        QuantityImpl actualDepth = (QuantityImpl)channel.getSite().getLocation().depth;
        boolean inDepthRange = actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth());
        if (inDepthRange) {
            return new StringTreeLeaf(this, true);
        } else {
            return new Fail(this, getMinDepth()+" <= "+actualDepth+" <= "+getMaxDepth());
        }
    }
}// SiteDepthRange
