package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.*;

/**
 * This subsetter is used to specify a negation of availableDataSubsetter. This subsetter is accepted only when the included
 * subsetter is false.
 *<pre>
 *  &lt;availableDataAND&gt;
 *      &lt;nogaps/&gt;
 *  &lt;/availableDataAND&gt;
 *</pre>
 */


public final class RequestSubsetterNOT
    extends  WaveformLogicalSubsetter
    implements RequestSubsetter {

    /**
     * Creates a new <code>RequestSubsetterNOT</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public RequestSubsetterNOT (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event,
              NetworkAccess network,
              Channel channel,
              RequestFilter[] original,
              CookieJar cookies) throws Exception{

    Iterator it = filterList.iterator();
    while (it.hasNext()) {
        RequestSubsetter filter = (RequestSubsetter)it.next();
        if (filter.accept(event, network, channel, original, cookies)) {
        return false;
        }
    }
    return true;
    }

}
