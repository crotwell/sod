package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * networkAttrNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 *
 * sample xml file
 *<body><pre><bold>
 * &lt;networkAttrNOT&gt;
 *  &lt;networkeffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *              &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkeffectiveTimeOverlap&gt;
 * &lt;/networkAttrNOT&gt;
 * </bold></pre></body>
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class NetworkNOT
    extends  NetworkLogicalSubsetter
    implements NetworkSubsetter {

    /**
     * Creates a new <code>NetworkAttrNOT</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public NetworkNOT (Element config) throws ConfigurationException {
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
    Iterator it = filterList.iterator();
    if(it.hasNext()) {
        NetworkSubsetter filter = (NetworkSubsetter)it.next();
        if ( filter.accept(net, cookies)) {
        return false;
        }
    }
    return true;
    }

}// NetworkAttrNOT
