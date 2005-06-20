package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;

public final class ChannelOR extends  ChannelLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel e, ProxyNetworkAccess network) throws Exception{
        Iterator it = subsetters.iterator();
        while(it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            if ( filter.accept(e, network)) { return true; }
        }
        return false;
    }

}// ChannelOR
