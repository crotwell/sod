package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * networkAttrXOR contains a sequence of channelSubsetters. The minimum value of the sequence is 2 and
 *the max value of the sequence is 2.
 *
 * sample xml file
 *<pre><bold>
 * &lt;networkAttrXOR&gt;
 *  &lt;networkeffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *              &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkeffectiveTimeOverlap&gt;
 *  &lt;networkeffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *              &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkeffectiveTimeOverlap&gt;
 * &lt;/networkAttrXOR&gt;
 *</pre></body>
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class NetworkXOR
    extends  NetworkLogicalSubsetter
    implements NetworkSubsetter {

    /**
     * Creates a new <code>NetworkAttrXOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public NetworkXOR (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e a <code>NetworkAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAttr net,  CookieJar cookies) throws Exception{
        NetworkSubsetter filterA = (NetworkSubsetter)filterList.get(0);
        NetworkSubsetter filterB = (NetworkSubsetter)filterList.get(1);
        return ( filterA.accept(net, cookies) != filterB.accept(net, cookies));

    }

}// NetworkAttrXOR
