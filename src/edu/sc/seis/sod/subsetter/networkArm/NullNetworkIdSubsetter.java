package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * NullNetworkIdSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  NullNetworkIdSubsetter implements NetworkIdSubsetter{

    /**
     * Describe <code>accept</code> method here.
     *
     * @param networkId a <code>NetworkId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkId networkId, CookieJar cookies) {

	return true;

    }
    
}// NullNetworkIdSubsetter
