package edu.sc.seis.sod;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * EventChannelSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:19:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventChannelSubsetter {

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
			  NetworkAccess network, 
			  Channel channel, 
			  CookieJar cookies) throws Exception;
    
}// EventChannelSubsetter
