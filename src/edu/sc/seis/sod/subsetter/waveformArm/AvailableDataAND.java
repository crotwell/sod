package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import java.util.Iterator;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

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

public final class AvailableDataAND extends  WaveformLogicalSubsetter
    implements AvailableDataSubsetter {

    public AvailableDataAND (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available, CookieJar cookieJar)
        throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
            if (filter.accept(event, channel, original, available, cookieJar) == false) {
                return false;
            }
        }
        return true;
    }

    static Category logger =
        Category.getInstance(AvailableDataAND.class.getName());

}// AvailableDataAND
