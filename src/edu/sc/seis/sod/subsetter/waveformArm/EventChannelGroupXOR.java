/**
 * EventChannelGroupXOR.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;



public class EventChannelGroupXOR extends WaveformLogicalSubsetter
    implements EventChannelGroupSubsetter {

    public EventChannelGroupXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, ChannelGroup channel, CookieJar cookieJar) throws Exception {
        EventChannelGroupSubsetter filterA = (EventChannelGroupSubsetter)filterList.get(0);
        EventChannelGroupSubsetter filterB = (EventChannelGroupSubsetter)filterList.get(1);
        return ( filterA.accept(event, channel, cookieJar) != filterB.accept(event, channel, cookieJar));
    }

}

