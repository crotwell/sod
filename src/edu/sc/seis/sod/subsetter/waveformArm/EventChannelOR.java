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
 * This subsetter is used to specify a sequence of EventChannelSubsetters. This subsetter is accepted when even one
 * of the subsetters forming the sequence is accepted. If all the subsetters in the sequence are not accepted then
 * the eventChannelOR is not accepted.
 *<pre>
 * &lt;eventChannelOR&gt;
 * &lt;/eventChannelOR&gt;
 *</pre>
 */

public final class EventChannelOR extends  WaveformLogicalSubsetter
    implements EventChannelSubsetter {

    public EventChannelOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o, Channel channel, CookieJar cookieJar)
        throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
            if (filter.accept(o, channel, cookieJar)) {
                System.out.println(filter);
                return true; }
        }
        return false;
    }
}// EventChannelOR
