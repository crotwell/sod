package edu.sc.seis.sod;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

/**
 * RequestGenerator.java
 *
 *
 * Created: Thu Dec 13 17:25:25 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface  RequestGenerator extends Subsetter{

    /**
     * Describe <code>generateRequest</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>RequestFilter[]</code> value
     * @exception Exception if an error occurs
     */
    public RequestFilter[] generateRequest(EventAccessOperations event, 
					   NetworkAccess network, 
					   Channel channel, 
					   CookieJar cookies) throws Exception;
    
}// RequestGenerator
