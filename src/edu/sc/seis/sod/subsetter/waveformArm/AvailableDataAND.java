package edu.sc.seis.sod.subsetter.waveFormArm;

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

public final class AvailableDataAND
    extends  WaveFormLogicalSubsetter
    implements AvailableDataSubsetter {

    /**
     * Creates a new <code>AvailableDataAND</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public AvailableDataAND (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event,
              NetworkAccess network,
              Channel channel,
              RequestFilter[] original,
              RequestFilter[] available,
              CookieJar cookies) throws Exception{
    Iterator it = filterList.iterator();
    while (it.hasNext()) {
        AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
        if (filter.accept(event, network, channel, original, available, cookies) == false) {
        return false;
        }
    }
    return true;
    }

    static Category logger =
    Category.getInstance(AvailableDataAND.class.getName());

}// AvailableDataAND
