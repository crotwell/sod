package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class OrientationDipRange extends RangeSubsetter implements
        ChannelSubsetter {

    public OrientationDipRange(Element config) {
        super(config);
    }

    public boolean accept(Channel e, NetworkAccess network) throws Exception {
        return accept(e.an_orientation.dip);
    }
}// OrientationDipRange
