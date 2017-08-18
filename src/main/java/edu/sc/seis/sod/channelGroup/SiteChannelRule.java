package edu.sc.seis.sod.channelGroup;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.station.ChannelGroup;


public abstract class SiteChannelRule {

    public SiteChannelRule() {
        // TODO Auto-generated constructor stub
    }
    
    public abstract List<ChannelGroup> acceptable(List<Channel> chanList, List<Channel> failures);
}
