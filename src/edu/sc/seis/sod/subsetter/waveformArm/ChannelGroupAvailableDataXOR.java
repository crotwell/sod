/**
 * AvailableDataGroupXOR.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;



public class ChannelGroupAvailableDataXOR extends WaveformLogicalSubsetter
    implements ChannelGroupAvailableDataSubsetter {

    public ChannelGroupAvailableDataXOR(Element config) throws ConfigurationException{
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          CookieJar cookieJar)
        throws Exception{
        ChannelGroupAvailableDataSubsetter filterA = (ChannelGroupAvailableDataSubsetter)filterList.get(0);
        ChannelGroupAvailableDataSubsetter filterB = (ChannelGroupAvailableDataSubsetter)filterList.get(1);
        return ( filterA.accept(event, channel, original, available, cookieJar) != filterB.accept(event, channel, original, available, cookieJar));
    }

}

