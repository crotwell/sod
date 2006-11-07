package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class AlwaysSuccess extends ChannelLogicalSubsetter implements ChannelSubsetter {

    public AlwaysSuccess(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        StringTree[] result = new StringTree[subsetters.size()];
        Iterator it = subsetters.iterator();
        int i = 0;
        while(it.hasNext()) {
            ChannelSubsetter cur = (ChannelSubsetter)it.next();
            result[i] = cur.accept(channel, network);
            if(!result[i].isSuccess()) {
                break;
            }
            i++;
        }
        return new Pass(this);
    }
}
