package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class OrientationDipRange extends RangeSubsetter implements
        ChannelSubsetter {

    public OrientationDipRange(Element config) {
        super(config);
    }

    public StringTree accept(Channel c, NetworkSource network) throws Exception {
        return new StringTreeLeaf(this, accept(c.getDip().getValue()));
    }
}// OrientationDipRange
