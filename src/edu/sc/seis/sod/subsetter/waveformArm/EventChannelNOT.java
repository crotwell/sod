package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 * Describe class <code>EventChannelNOT</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class EventChannelNOT
    extends  WaveFormLogicalSubsetter
    implements EventChannelSubsetter {

    /**
     * Creates a new <code>EventChannelNOT</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventChannelNOT (Element config) throws ConfigurationException {
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
    public boolean accept(EventAccessOperations o,  NetworkAccess network,Channel channel,  CookieJar cookies)
    throws Exception{
    Iterator it = filterList.iterator();
    while (it.hasNext()) {
        EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
        if (!filter.accept(o, network, channel, cookies)) {
        return false;
        }
    }
    return true;
    }

}// EventChannelAND
