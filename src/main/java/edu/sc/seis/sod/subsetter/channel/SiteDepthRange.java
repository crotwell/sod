package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SiteDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements ChannelSubsetter {

    public SiteDepthRange(Element config) throws Exception {
        super(config);
    }

    public StringTree accept(Channel channel, NetworkSource network)
            throws Exception {
        QuantityImpl actualDepth = new QuantityImpl(channel.getDepth().getValue(), UnitImpl.METER);
        return new StringTreeLeaf(this, actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth()));
    }
}// SiteDepthRange
