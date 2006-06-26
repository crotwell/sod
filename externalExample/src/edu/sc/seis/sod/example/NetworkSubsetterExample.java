package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;


public class NetworkSubsetterExample implements NetworkSubsetter {

    public boolean accept(NetworkAttr attr) throws Exception {
        return true;
    }
}
