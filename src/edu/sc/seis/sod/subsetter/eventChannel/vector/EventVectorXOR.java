/**
 * EventChannelGroupXOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class EventVectorXOR extends EventVectorLogicalSubsetter implements
        EventVectorSubsetter {

    public EventVectorXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception {
        EventVectorSubsetter filterA = (EventVectorSubsetter)filterList.get(0);
        EventVectorSubsetter filterB = (EventVectorSubsetter)filterList.get(1);
        return (filterA.accept(event, channel, cookieJar) != filterB.accept(event,
                                                                            channel,
                                                                            cookieJar));
    }
}