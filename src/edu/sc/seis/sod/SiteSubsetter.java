package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * SiteSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:05:33 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface SiteSubsetter extends Subsetter{

    public boolean accept(NetworkAccessOperations network,
			  Site station, 
			  CookieJar cookies);
    
}// SiteSubsetter
