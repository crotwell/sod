package edu.sc.seis.sod.subsetter.network;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

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
public final class NetworkNOT extends  NetworkLogicalSubsetter
    implements NetworkSubsetter {

    public NetworkNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(NetworkAttr net) throws Exception{
        Iterator it = filterList.iterator();
        if(it.hasNext()) {
            NetworkSubsetter filter = (NetworkSubsetter)it.next();
            if ( filter.accept(net)) { return false; }
        }
        return true;
    }
}// NetworkAttrNOT
