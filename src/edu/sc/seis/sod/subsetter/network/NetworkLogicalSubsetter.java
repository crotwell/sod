package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

public class NetworkLogicalSubsetter extends LogicalSubsetter {

    public NetworkLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public NetworkSubsetter[] getSubsetters() {
        return (NetworkSubsetter[])filterList.toArray(new NetworkSubsetter[0]);
    }

    public String getArmName() {
        return "network";
    }
}// NetworkLogicalSubsetter
