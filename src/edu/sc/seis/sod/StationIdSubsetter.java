package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * StationIdSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:06:22 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface StationIdSubsetter extends Subsetter {

    /**
     * Describe <code>accept</code> method here.
     *
     * @param id a <code>StationId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(StationId id, CookieJar cookies);
    
}// StationIdSubsetter
