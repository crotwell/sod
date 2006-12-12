package edu.sc.seis.sod.subsetter.network;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;

public abstract class NetworkLogicalSubsetter extends LogicalSubsetter {

    public NetworkLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public NetworkSubsetter[] getSubsetters() {
        return (NetworkSubsetter[])filterList.toArray(new NetworkSubsetter[0]);
    }

    public String getPackage() {
        return "network";
    }

    public StringTree accept(NetworkAttr net) throws Exception {
        List reasons = new ArrayList(filterList.size());
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            NetworkSubsetter processor = (NetworkSubsetter)it.next();
            StringTree result;
            synchronized(processor) {
                result = processor.accept(net);
            }
            reasons.add(result);
            if(!shouldContinue(result)) {
                break;
            }
        } // end of while (it.hasNext())
        StringTree[] results = (StringTree[])reasons.toArray(new StringTree[0]);
        return new StringTreeBranch(this, isSuccess(results), results);
    }

    public abstract boolean shouldContinue(StringTree result);

    public abstract boolean isSuccess(StringTree[] reasons);
}// NetworkLogicalSubsetter
