package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * StationSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:05:33 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface StationSubsetter extends Subsetter{

    public boolean accept(NetworkAccess network,
			  Station station,
			  CookieJar cookies) throws Exception;
    
}// StationSubsetter
