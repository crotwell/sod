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
import org.apache.log4j.*;

/**
 * availableDataAND contains a sequence of availableSubsetters. The minimum value of the sequence is 0 and
 * the max value of the sequence is unLimited.
 *<pre>
 *  &lt;availableDataAND&gt;
 *      &lt;nogaps/&gt;
 *      &lt;fullCoverage/&gt;
 *  &lt;/availableDataAND&gt;
 *</pre>
 */

public final class RequestSubsetterAND
    extends  WaveformLogicalSubsetter
    implements RequestSubsetter {

    /**
     * Creates a new <code>RequestSubsetterAND</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public RequestSubsetterAND (Element config) throws ConfigurationException {
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
              Channel channel,
              RequestFilter[] original, CookieJar cookieJar) throws Exception{
    Iterator it = filterList.iterator();
    while (it.hasNext()) {
        RequestSubsetter filter = (RequestSubsetter)it.next();
        if (filter.accept(event, channel, original, cookieJar) == false) {
        return false;
        }
    }
    return true;
    }

    static Category logger =
    Category.getInstance(RequestSubsetterAND.class.getName());

}// RequestSubsetterAND
