package edu.sc.seis.sod.subsetter.origin;
import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import org.w3c.dom.Element;

public class EventLogicalSubsetter extends LogicalSubsetter{
    public EventLogicalSubsetter (Element config) throws ConfigurationException{
        super(config);
    }
    
    public String getArmName() { return "eventArm"; }
}// EventLogicalSubsetter
