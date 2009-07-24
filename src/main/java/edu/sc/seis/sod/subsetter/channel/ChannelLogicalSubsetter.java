package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

/**
 * @author groves Created on Aug 30, 2004
 */
public abstract class ChannelLogicalSubsetter extends CompositeChannelSubsetter {

    public ChannelLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel e, ProxyNetworkAccess network)
            throws Exception {
        List reasons = new ArrayList(subsetters.size());
        Iterator it = subsetters.iterator();
        while(it.hasNext()) {
            ChannelSubsetter processor = (ChannelSubsetter)it.next();
            StringTree result;
            synchronized(processor) {
                result = processor.accept(e, network);
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
}