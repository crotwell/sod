package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * SiteIdSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:06:22 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface SiteIdSubsetter extends Subsetter {

    public boolean accept(SiteId id, CookieJar cookies) throws Exception;
    
}// SiteIdSubsetter
