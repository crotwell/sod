package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;


import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * NullNetworkAttrSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  NullNetworkSubsetter implements NetworkSubsetter{

    /**
     * Describe <code>accept</code> method here.
     *
     * @param networkAttr a <code>NetworkAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAttr networkAttr, CookieJar cookies) {

    return true;

    }

}// NullNetworkAttrSubsetter
