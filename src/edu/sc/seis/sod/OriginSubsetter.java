package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;

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
    public boolean accept(EventAccessOperations eventAccess,
			  Origin origin, 
			  CookieJar cookies) throws Exception;
    
}// OriginSubsetter
