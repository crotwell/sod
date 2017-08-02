package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.bag.AreaUtil;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

public class SitePolygonFile implements ChannelSubsetter {

    public SitePolygonFile(Element el) throws ConfigurationException {
        locs = AreaSubsetter.extractPolygon(DOMHelper.extractText(el, "."));
    }

    private Location[] locs;

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        return new StringTreeLeaf(this, AreaUtil.inArea(locs, channel.getSite().getLocation()));
    }
}
