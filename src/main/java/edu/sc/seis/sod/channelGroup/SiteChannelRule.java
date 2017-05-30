package edu.sc.seis.sod.channelGroup;

import java.util.List;

import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelImpl;


public abstract class SiteChannelRule {

    public SiteChannelRule() {
        // TODO Auto-generated constructor stub
    }
    
    public abstract List<ChannelGroup> acceptable(List<ChannelImpl> chanList, List<ChannelImpl> failures);
}
