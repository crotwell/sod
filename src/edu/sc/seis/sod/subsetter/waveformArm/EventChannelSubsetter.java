package edu.sc.seis.sod.subsetter.waveformArm;
import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;

/**
 * EventChannelSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:19:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventChannelSubsetter  extends Subsetter {

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event,
              Channel channel, CookieJar cookieJar) throws Exception;

}// EventChannelSubsetter
