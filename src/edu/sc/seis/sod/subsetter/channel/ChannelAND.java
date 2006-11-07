package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class ChannelAND extends  ChannelLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelAND (Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel e, ProxyNetworkAccess network) throws Exception{
        StringTree[] result = new StringTree[subsetters.size()];
        Iterator it = subsetters.iterator();
        int i=0;
        while(it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            result[i] = filter.accept(e, network);
            if ( !result[i].isSuccess()) { return new StringTreeBranch(this, false, result); }
            i++;
        }
        return new StringTreeBranch(this, true, result);
    }
}// ChannelAND
