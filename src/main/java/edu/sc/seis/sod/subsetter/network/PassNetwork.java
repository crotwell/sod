package edu.sc.seis.sod.subsetter.network;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * PassNetwork.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author Philip Crotwell
 */

public class  PassNetwork implements NetworkSubsetter{

    public StringTree accept(Network networkAttr) {
        return new Pass(this);
    }

}// PassNetwork
