package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

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

public class  NullNetworkAttrSubsetter implements NetworkAttrSubsetter{

    public boolean accept(NetworkAttr networkAttr, CookieJar cookies) {

	return true;

    }
    
}// NullNetworkAttrSubsetter
