package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * ChannelIdSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:15:04 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface ChannelIdSubsetter extends Subsetter {

    /**
     * Describe <code>accept</code> method here.
     *
     * @param channelId a <code>ChannelId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(ChannelId channelId, CookieJar cookies) throws Exception;
    
}// ChannelIdSubsetter
