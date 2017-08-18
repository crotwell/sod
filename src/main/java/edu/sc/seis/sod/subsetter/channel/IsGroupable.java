package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ChannelGrouper;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class IsGroupable implements ChannelSubsetter {

    public IsGroupable(Element config) {
    }
    
	public StringTree accept(Channel channel, NetworkSource network)
			throws Exception {
	    List<? extends Channel> allChans = network.getChannels((Station)channel.getStation());
        ArrayList<Channel> siteChans = new ArrayList<Channel>();
        for (Channel channelImpl : allChans) {
            siteChans.add(channelImpl);
		}

        List<Channel> failures = new ArrayList<Channel>();
        List<ChannelGroup> chanGroups = getChannelGrouper().group(siteChans, failures);
        for(ChannelGroup cg : chanGroups) {
        	if (cg.contains(channel)) {
        		return new Pass(this);
        	}
		}
        return new Fail(this);
	}


    
    public ChannelGrouper getChannelGrouper() {
        // lazy load
        if(channelGrouper == null) {
            channelGrouper = Start.getNetworkArm().getChannelGrouper();
        }
        return channelGrouper;
    }


    private ChannelGrouper channelGrouper;
}
