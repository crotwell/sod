package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;
import org.apache.log4j.Category;
import org.w3c.dom.Element;


/**
 * specifies the ChannelEffectiveTimeOverlap
 * <pre>
 *  &lt;channelEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/channelEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *      &lt;channelEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/channelEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;channelEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/channelEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;channelEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/channelEffectiveTimeOverlap&gt;
 * </pre>
 */



public class ChannelEffectiveTimeOverlap extends
    EffectiveTimeOverlap implements ChannelSubsetter {
    public ChannelEffectiveTimeOverlap (Element config){
        super(config);
    }

    public boolean accept(Channel channel) {
        return overlaps(channel.effective_time);
    }

    static Category logger =
        Category.getInstance(ChannelEffectiveTimeOverlap.class.getName());
}// ChannelEffectiveTimeOverlap
