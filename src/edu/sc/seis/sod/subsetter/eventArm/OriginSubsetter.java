package edu.sc.seis.sod.subsetter.eventArm;
import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;

/**
 * OriginSubsetter.java
 *
 *
 * Created: Tue Apr  2 13:32:13 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public interface OriginSubsetter extends Subsetter{
    /**
     * Describe <code>accept</code> method here.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations eventAccess,
              Origin origin,
              CookieJar cookies) throws Exception;
    
}// OriginSubsetter
