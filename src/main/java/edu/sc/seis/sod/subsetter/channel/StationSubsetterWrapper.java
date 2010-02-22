package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;


public class StationSubsetterWrapper implements ChannelSubsetter {

    public StationSubsetterWrapper(StationSubsetter staSub) {
        this.staSub = staSub;
    }

    public StringTree accept(ChannelImpl channel, ProxyNetworkAccess network)
            throws Exception {
        return staSub.accept((StationImpl)channel.getSite().getStation(), network);
    }
    
    StationSubsetter staSub;
}
