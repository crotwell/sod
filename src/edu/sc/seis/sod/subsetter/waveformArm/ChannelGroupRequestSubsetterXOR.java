/**
 * ChannelGroupRequestSubsetterXOR.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;
import edu.sc.seis.sod.ChannelGroup;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;



public class ChannelGroupRequestSubsetterXOR extends WaveformLogicalSubsetter
    implements ChannelGroupRequestSubsetter {


    public ChannelGroupRequestSubsetterXOR(Element config) throws ConfigurationException{
        super(config);
    }

    public boolean accept(EventAccessOperations event, ChannelGroup channel, RequestFilter[][] request, CookieJar cookieJar) throws Exception {
        ChannelGroupRequestSubsetter filterA = (ChannelGroupRequestSubsetter)filterList.get(0);
        ChannelGroupRequestSubsetter filterB = (ChannelGroupRequestSubsetter)filterList.get(1);
        return ( filterA.accept(event, channel, request, cookieJar) != filterB.accept(event, channel, request, cookieJar));
    }

}

