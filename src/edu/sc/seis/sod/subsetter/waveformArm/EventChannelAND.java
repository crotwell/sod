package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * eventChannelAND contains a sequence of eventChannelSubsetters. The minimum value of the sequence is 0 and
 * the max value of the sequence is unLimited.
 *<pre>
 * &lt;eventChannelAND&gt;
 * &lt;/eventChannelAND&gt;
 *</pre>
 */


public final class EventChannelAND
    extends  WaveformLogicalSubsetter
    implements EventChannelSubsetter {

    /**
     * Creates a new <code>EventChannelAND</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventChannelAND (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations o, NetworkAccess  network,  Channel channel,  CookieJar cookies)
    throws Exception{
    Iterator it = filterList.iterator();
    while (it.hasNext()) {
        EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
        if (!filter.accept(o, network, channel, cookies)) {
        return false;
        }
    }
    return true;
    }

}// EventChannelAND
