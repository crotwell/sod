package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;

public final class ChannelXOR extends ChannelLogicalSubsetter implements
        ChannelSubsetter {

    public ChannelXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel channel) throws Exception {
        ChannelSubsetter filterA = (ChannelSubsetter)subsetters.get(0);
        ChannelSubsetter filterB = (ChannelSubsetter)subsetters.get(1);
        return (filterA.accept(channel) != filterB.accept(channel));
    }
}// ChannelXOR
