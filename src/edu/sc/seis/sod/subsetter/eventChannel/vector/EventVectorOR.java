/**
 * EventChannelGroupOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class EventVectorOR extends EventVectorLogicalSubsetter implements
        EventVectorSubsetter {

    public EventVectorOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            EventVectorSubsetter filter = (EventVectorSubsetter)it.next();
            if(!filter.accept(event, channel, cookieJar)) { return false; }
        }
        return true;
    }
}