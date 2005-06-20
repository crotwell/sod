package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class OrientationDipRange extends RangeSubsetter implements
        ChannelSubsetter {

    public OrientationDipRange(Element config) {
        super(config);
    }

    public boolean accept(Channel e, ProxyNetworkAccess network) throws Exception {
        return accept(e.an_orientation.dip);
    }
}// OrientationDipRange
