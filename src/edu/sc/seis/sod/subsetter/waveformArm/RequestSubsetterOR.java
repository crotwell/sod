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
 * This subsetter is used to specify a sequence of RequestSubsetters. This subsetter is accepted when even one
 * of the subsetters forming the sequence is accepted. If all the subsetters in the sequence are not accepted then
 * the availableDataOR is not accepted.
 *<pre>
 *  &lt;availableDataOR&gt;
 *      &lt;nogaps/&gt;
 *      &lt;fullCoverage/&gt;
 *  &lt;/availableDataOR&gt;
 *</pre>
 */

public final class RequestSubsetterOR
    extends  WaveformLogicalSubsetter
    implements RequestSubsetter {

    /**
     * Creates a new <code>RequestSubsetterOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public RequestSubsetterOR (Element config) throws ConfigurationException {
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
            if (filter.accept(event, channel, original, cookieJar)) {
                return true;
            }
        }
        return false;
    }

}// RequestSubsetterOR

