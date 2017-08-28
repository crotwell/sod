package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.bag.AreaUtil;
import edu.sc.seis.sod.model.common.BoxAreaImpl;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SiteBoxArea implements ChannelSubsetter {

    public SiteBoxArea(Element el) throws ConfigurationException {
        this.ba = SodUtil.loadBoxArea(el);
    }

    public StringTree accept(Channel chan, NetworkSource network) {
        return new StringTreeLeaf(this, AreaUtil.inArea(ba, Location.of(chan)));
    }

    private BoxAreaImpl ba;
}
