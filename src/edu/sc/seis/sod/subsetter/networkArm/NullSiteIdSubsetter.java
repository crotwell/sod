package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * NullSiteIdSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  NullSiteIdSubsetter implements SiteIdSubsetter{

    /**
     * Describe <code>accept</code> method here.
     *
     * @param siteId a <code>SiteId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(SiteId siteId, CookieJar cookies) {

	return true;

    }
    
}// NullSiteIdSubsetter
