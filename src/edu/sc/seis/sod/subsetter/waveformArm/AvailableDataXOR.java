package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;

/**
 * eventAttrXOR contains a sequence of eventAttrSubsetters. The minimum value of the sequence is 2 and
 * the max value of the sequence is 2.
 *<pre>
 *  &lt;availableDataXOR&gt;
 *      &lt;nogaps/&gt;
 *      &lt;fullCoverage/&gt;
 *  &lt;/availableDataXOR&gt;
 *</pre>
 */
public final class AvailableDataXOR extends  WaveformLogicalSubsetter
    implements AvailableDataSubsetter {

    public AvailableDataXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available)
        throws Exception{
        AvailableDataSubsetter filterA = (AvailableDataSubsetter)filterList.get(0);
        AvailableDataSubsetter filterB = (AvailableDataSubsetter)filterList.get(1);
        return ( filterA.accept(event, channel, original, available) != filterB.accept(event, channel, original, available));

    }

}// AvailableDataXOR
