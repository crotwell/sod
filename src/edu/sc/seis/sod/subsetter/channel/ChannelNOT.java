package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public final class ChannelNOT extends  ChannelLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel e, ProxyNetworkAccess network) throws Exception{
        Iterator it = subsetters.iterator();
        if (it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            StringTree ans = filter.accept(e, network);
            return new StringTreeBranch(this, ! ans.isSuccess(), ans); 
        }
        return new StringTreeLeaf(this, true);
    }

}// ChannelNOT
