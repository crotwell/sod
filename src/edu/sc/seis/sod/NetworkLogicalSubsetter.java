package edu.sc.seis.sod;

import org.w3c.dom.Element;

public class NetworkLogicalSubsetter extends LogicalSubsetter{
    public NetworkLogicalSubsetter (Element config) throws ConfigurationException{
        super(config);
    }
    
    public String getArmName() { return "networkArm"; }
}// NetworkLogicalSubsetter
