package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * This subsetter is used to specify a negation of availableDataSubsetter. This subsetter is accepted only when the included
 * subsetter is false.
 *<pre>
 *  &lt;availableDataAND&gt;
 *      &lt;nogaps/&gt;
 *  &lt;/availableDataAND&gt;
 *</pre>
 */


public final class AvailableDataNOT extends  WaveformLogicalSubsetter
    implements AvailableDataSubsetter {

    public AvailableDataNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,  Channel channel,
                          RequestFilter[] original, RequestFilter[] available, CookieJar cookieJar)
        throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
            if (filter.accept(event, channel, original, available, cookieJar)) {
                return false;
            }
        }
        return true;
    }

}// AvailableDataNOT
