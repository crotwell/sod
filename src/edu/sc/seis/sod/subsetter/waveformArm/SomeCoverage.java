package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

/**
 * sample xml
 *<pre>
 *&lt;someCoverage/&gt;
 *</pre>
 */

public class SomeCoverage implements AvailableDataSubsetter, SodElement{
    /**
     * Creates a new <code>SomeCoverage</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SomeCoverage (Element config){

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
     */
    public boolean accept(EventAccessOperations event,
              Channel channel,
              RequestFilter[] original,
              RequestFilter[] available, CookieJar cookieJar) {
    // simple impl, probably need more robust
    if (available != null && available.length != 0) {
        return true;
    }

    return false;
    }

    static Category logger =
    Category.getInstance(SomeCoverage.class.getName());

}// SomeCoverage
