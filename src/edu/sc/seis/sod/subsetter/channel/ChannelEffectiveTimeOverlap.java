package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

public class ChannelEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        ChannelSubsetter {

    public ChannelEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel channel) {
        return overlaps(channel.effective_time);
    }

    static Category logger = Category.getInstance(ChannelEffectiveTimeOverlap.class.getName());
}// ChannelEffectiveTimeOverlap
