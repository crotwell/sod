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

    /**
     * Describe <code>accept</code> method here.
     *
     * @param id a <code>SiteId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(SiteId id, CookieJar cookies) throws Exception;
    
}// SiteIdSubsetter
