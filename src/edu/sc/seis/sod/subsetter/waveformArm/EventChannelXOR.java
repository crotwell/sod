package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 * Describe class <code>EventChannelXOR</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class EventChannelXOR
    extends  WaveFormLogicalSubsetter
    implements EventChannelSubsetter {

    /**
     * Creates a new <code>EventChannelXOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventChannelXOR (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event,
                          NetworkAccess network,
                          Channel channel,
                          CookieJar cookies)
    throws Exception{
        EventChannelSubsetter filterA = (EventChannelSubsetter)filterList.get(0);
        EventChannelSubsetter filterB = (EventChannelSubsetter)filterList.get(1);
        return ( filterA.accept(event, network, channel, cookies) != filterB.accept(event, network, channel, cookies));

    }

}// EventChannelXOR
