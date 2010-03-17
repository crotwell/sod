package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class OrientationAzimuthRange extends RangeSubsetter implements
        ChannelSubsetter {

    public OrientationAzimuthRange(Element config) {
        super(config);
        if(min > 180) min = min - 360;
        if(max > 180) max = max - 360;
    }

    public StringTree accept(ChannelImpl e, NetworkSource network) throws Exception {
        return new StringTreeLeaf(this, accept(e.getOrientation().azimuth));
    }
}// OrientationAzimuthRange
