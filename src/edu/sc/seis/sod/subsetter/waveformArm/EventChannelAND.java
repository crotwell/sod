package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * eventChannelAND contains a sequence of eventChannelSubsetters. The minimum value of the sequence is 0 and
 * the max value of the sequence is unLimited.
 *<pre>
 * &lt;eventChannelAND&gt;
 * &lt;/eventChannelAND&gt;
 *</pre>
 */


public final class EventChannelAND extends  WaveformLogicalSubsetter
    implements EventChannelSubsetter {

    public EventChannelAND (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o, Channel channel)
        throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
            if (!filter.accept(o, channel)) { return false; }
        }
        return true;
    }
}// EventChannelAND
