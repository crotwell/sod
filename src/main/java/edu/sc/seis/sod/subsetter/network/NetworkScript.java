package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.network.VelocityNetwork;


public class NetworkScript extends AbstractScriptSubsetter implements NetworkSubsetter {

    public NetworkScript(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    public StringTree accept(Network network) throws Exception {
        return runScript(new VelocityNetwork(network));
    }
    
    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityNetwork networkAttr) throws Exception {
        engine.put("networkAttr", networkAttr);
        return eval();
    }
}
