package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkAttr;

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

    public boolean accept(NetworkAttr networkAttr) {
        return true;
    }

}// NullNetworkAttrSubsetter
