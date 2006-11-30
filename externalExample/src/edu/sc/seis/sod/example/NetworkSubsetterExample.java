package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;


public class NetworkSubsetterExample implements NetworkSubsetter {

    public StringTree accept(NetworkAttr attr) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}
