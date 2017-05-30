package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class OrientationDipRange extends RangeSubsetter implements
        ChannelSubsetter {

    public OrientationDipRange(Element config) {
        super(config);
    }

    public StringTree accept(ChannelImpl e, NetworkSource network) throws Exception {
        return new StringTreeLeaf(this, accept(e.getOrientation().dip));
    }
}// OrientationDipRange
