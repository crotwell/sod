package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.network.*;

/**
 * AvailableDataSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:18:32 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface AvailableDataSubsetter {

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  RequestFilter[] original, 
			  RequestFilter[] available, 
			  CookieJar cookies) throws Exception;
    
}// AvailableDataSubsetter
