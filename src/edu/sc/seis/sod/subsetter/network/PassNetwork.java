package edu.sc.seis.sod.subsetter.network;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

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

    public StringTree accept(NetworkAttr networkAttr) {
        return new Pass(this);
    }

}// PassNetwork
