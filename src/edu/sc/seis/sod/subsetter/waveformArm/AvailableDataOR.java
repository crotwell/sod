package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import java.util.Iterator;
import org.w3c.dom.Element;


/**
 * This subsetter is used to specify a sequence of AvailableDataSubsetters. This subsetter is accepted when even one
 * of the subsetters forming the sequence is accepted. If all the subsetters in the sequence are not accepted then
 * the availableDataOR is not accepted.
 *<pre>
 *  &lt;availableDataOR&gt;
 *      &lt;nogaps/&gt;
 *      &lt;fullCoverage/&gt;
 *  &lt;/availableDataOR&gt;
 *</pre>
 */

public final class AvailableDataOR extends  WaveformLogicalSubsetter
    implements AvailableDataSubsetter {

    public AvailableDataOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available, CookieJar cookieJar)
        throws Exception{

        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
            if (filter.accept(event, channel, original, available, cookieJar)) {
                return true;
            }
        }
        return false;
    }

}// AvailableDataOR
