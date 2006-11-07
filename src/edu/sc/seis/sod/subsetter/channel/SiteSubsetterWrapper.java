package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.site.SiteSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class SiteSubsetterWrapper implements ChannelSubsetter {

    public SiteSubsetterWrapper(SiteSubsetter s) {
        this.s = s;
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network) throws Exception {
        return new StringTreeLeaf(this, s.accept(channel.my_site, network));
    }

    private SiteSubsetter s;
}