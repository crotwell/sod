package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ChannelGrouper;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class IsGroupable implements ChannelSubsetter {

	public StringTree accept(Channel channel, ProxyNetworkAccess network)
			throws Exception {
        Channel[] allChans = network.retrieve_for_station(channel.getSite().getStation().get_id());
        ArrayList<ChannelImpl> siteChans = new ArrayList<ChannelImpl>();
        for (int i = 0; i < allChans.length; i++) {
			if (SiteIdUtil.areSameSite(allChans[i].get_id(), channel.get_id())) {
				siteChans.add((ChannelImpl)allChans[i]);
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
