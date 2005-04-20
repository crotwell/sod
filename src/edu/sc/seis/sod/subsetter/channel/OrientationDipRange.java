package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class OrientationDipRange extends RangeSubsetter implements
        ChannelSubsetter {

    public OrientationDipRange(Element config) {
        super(config);
    }

    public boolean accept(Channel e) throws Exception {
        return accept(e.an_orientation.dip);
    }
}// OrientationDipRange
