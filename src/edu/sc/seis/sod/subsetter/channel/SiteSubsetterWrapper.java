package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.subsetter.site.SiteSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class SiteSubsetterWrapper implements ChannelSubsetter {

    public SiteSubsetterWrapper(SiteSubsetter s) {
        this.s = s;
    }

    public boolean accept(Channel channel) throws Exception {
        return s.accept(channel.my_site);
    }

    private SiteSubsetter s;
}