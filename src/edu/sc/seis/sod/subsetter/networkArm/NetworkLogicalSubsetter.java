package edu.sc.seis.sod.subsetter.networkArm;
import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import org.w3c.dom.Element;

public class NetworkLogicalSubsetter extends LogicalSubsetter{
    public NetworkLogicalSubsetter (Element config) throws ConfigurationException{
        super(config);
    }
    
    public String getArmName() { return "networkArm"; }
}// NetworkLogicalSubsetter
