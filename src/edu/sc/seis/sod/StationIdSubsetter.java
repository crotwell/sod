package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * StationIdFilter.java
 *
 *
 * Created: Thu Dec 13 17:06:22 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface StationIdFilter {

    public boolean accept(StationId id, CookieJar cookies);
    
}// StationIdFilter
