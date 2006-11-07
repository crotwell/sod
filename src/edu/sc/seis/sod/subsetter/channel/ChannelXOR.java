package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class ChannelXOR extends ChannelLogicalSubsetter implements
        ChannelSubsetter {

    public ChannelXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network) throws Exception {
        ChannelSubsetter filterA = (ChannelSubsetter)subsetters.get(0);
        ChannelSubsetter filterB = (ChannelSubsetter)subsetters.get(1);
        StringTree resultA = filterA.accept(channel, null);
        StringTree resultB = filterB.accept(channel, null);
        return new StringTreeBranch(this, resultA.isSuccess() != resultB.isSuccess(), new StringTree[] {resultA, resultB});
    }
}// ChannelXOR
