package edu.sc.seis.sod.channelGroup;

import java.util.List;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;


public abstract class SiteChannelRule {

    public SiteChannelRule() {
        // TODO Auto-generated constructor stub
    }
    
    public abstract List<ChannelGroup> acceptable(List<ChannelImpl> chanList, List<ChannelImpl> failures);
}
