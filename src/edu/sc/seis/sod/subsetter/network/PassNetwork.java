package edu.sc.seis.sod.subsetter.network;

import edu.iris.Fissures.IfNetwork.NetworkAttr;

/**
 * PassNetwork.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  PassNetwork implements NetworkSubsetter{

    public boolean accept(NetworkAttr networkAttr) {
        return true;
    }

}// PassNetwork
