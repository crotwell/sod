package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SiteBoxArea implements ChannelSubsetter {

    public SiteBoxArea(Element el) throws ConfigurationException {
        this.ba = SodUtil.loadBoxArea(el);
    }

    public StringTree accept(Channel chan, ProxyNetworkAccess network) {
        return new StringTreeLeaf(this, AreaUtil.inArea(ba, chan.my_site.my_location));
    }

    private BoxArea ba;
}
