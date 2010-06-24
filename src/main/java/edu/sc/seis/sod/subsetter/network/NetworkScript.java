package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class NetworkScript extends AbstractScriptSubsetter implements NetworkSubsetter {

    public NetworkScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(NetworkAttr attr) throws Exception {
        engine.put("networkAttr", attr);
        return eval();
    }
}
