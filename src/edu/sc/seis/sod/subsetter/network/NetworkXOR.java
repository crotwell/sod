package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;

public final class NetworkXOR extends NetworkLogicalSubsetter implements
        NetworkSubsetter {

    public NetworkXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(NetworkAttr net) throws Exception {
        NetworkSubsetter filterA = (NetworkSubsetter)filterList.get(0);
        NetworkSubsetter filterB = (NetworkSubsetter)filterList.get(1);
        return (filterA.accept(net) != filterB.accept(net));
    }
}// NetworkAttrXOR
