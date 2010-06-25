package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.network.VelocityNetwork;


public class NetworkScript extends AbstractScriptSubsetter implements NetworkSubsetter {

    public NetworkScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(NetworkAttrImpl network) throws Exception {
        engine.put("networkAttr", new VelocityNetwork(network));
        return eval();
    }
}
