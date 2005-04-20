package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class OrientationAzimuthRange extends RangeSubsetter implements
        ChannelSubsetter {

    public OrientationAzimuthRange(Element config) {
        super(config);
        if(min > 180) min = min - 360;
        if(max > 180) max = max - 360;
    }

    public boolean accept(Channel e) throws Exception {
        return accept(e.an_orientation.azimuth);
    }
}// OrientationAzimuthRange
