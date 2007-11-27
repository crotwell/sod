package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class StationSubsetterWrapper implements ChannelSubsetter {

    public StationSubsetterWrapper(StationSubsetter sub) {
        this.sub = sub;
    }

    public StringTree accept(Channel chan, ProxyNetworkAccess network) throws Exception {
        return sub.accept(chan.my_site.my_station, network);
    }

    private StationSubsetter sub;
}