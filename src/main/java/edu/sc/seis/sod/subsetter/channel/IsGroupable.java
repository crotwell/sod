package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ChannelGrouper;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class IsGroupable implements ChannelSubsetter {

	public StringTree accept(ChannelImpl channel, NetworkSource network)
			throws Exception {
	    List<? extends ChannelImpl> allChans = network.getChannels((StationImpl)channel.getStation());
        ArrayList<ChannelImpl> siteChans = new ArrayList<ChannelImpl>();
        for (ChannelImpl channelImpl : allChans) {
			if (SiteIdUtil.areSameSite(channelImpl.get_id(), channel.get_id())) {
				siteChans.add(channelImpl);
			}
		}

        List<ChannelImpl> failures = new ArrayList<ChannelImpl>();
        List<ChannelGroup> chanGroups = channelGrouper.group(siteChans, failures);
        for(ChannelGroup cg : chanGroups) {
        	if (cg.contains(channel)) {
        		return new Pass(this);
        	}
		}
        return new Fail(this);
	}


    private ChannelGrouper channelGrouper = new ChannelGrouper();
}
