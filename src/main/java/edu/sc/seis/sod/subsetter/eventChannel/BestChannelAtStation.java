package edu.sc.seis.sod.subsetter.eventChannel;

import java.util.List;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.BestChannelUtil;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;


public class BestChannelAtStation implements EventChannelSubsetter, EventVectorSubsetter {

    public StringTree accept(CacheEvent event, ChannelImpl channel, CookieJar cookieJar) throws Exception {
        String bandCode = channel.get_code().substring(0, 1);
        List<ChannelImpl> staChans = Start.getNetworkArm().getSuccessfulChannels(((StationImpl)channel.getStation()));
        Channel[] allChannels = staChans.toArray(new Channel[0]);
        allChannels = BestChannelUtil.pruneChannels(allChannels, event.getOrigin().getTime());
        Channel[] bestChannels = BestChannelUtil.getBestMotionVector(allChannels, bandCode);
        if (bestChannels == null) {
            bestChannels = BestChannelUtil.getChannels(allChannels, bandCode);
        }
        if (bestChannels == null) {
            return new Fail(this, "No best channels");
        }
        for (int i = 0; i < bestChannels.length; i++) {
            if (ChannelIdUtil.areEqual(channel, ((ChannelImpl)bestChannels[i]))) {
                return new Pass(this);
            }
        }
        return new Fail(this);
    }

    public StringTree accept(CacheEvent event, ChannelGroup channel, CookieJar cookieJar) throws Exception {
        String bandCode = channel.getChannel1().get_code().substring(0, 1);
        List<ChannelGroup> staChans = Start.getNetworkArm().getSuccessfulChannelGroups(((StationImpl)channel.getStation()));
        Channel[] allChannels = new Channel[staChans.size()*3];
        int i=0;
        for (ChannelGroup cg : staChans) {
            ChannelImpl[] cgChans = cg.getChannels();
            allChannels[i++] = cgChans[0];
            allChannels[i++] = cgChans[1];
            allChannels[i++] = cgChans[2];
        }
        allChannels = BestChannelUtil.pruneChannels(allChannels, event.getOrigin().getTime());
        Channel[] bestChannels = BestChannelUtil.getBestMotionVector(allChannels);
        if (bestChannels == null) {
            return new Fail(this, "No best channel group");
        }
        ChannelGroup best = new ChannelGroup(ChannelImpl.implize(bestChannels));
        if (best.areEqual(channel)) {
            return new Pass(this);
        }
        return new Fail(this);
    }
}
