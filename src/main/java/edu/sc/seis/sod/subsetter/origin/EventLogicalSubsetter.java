package edu.sc.seis.sod.subsetter.origin;
import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

public class EventLogicalSubsetter extends LogicalSubsetter{
    public EventLogicalSubsetter (Element config) throws ConfigurationException{
        super(config);
    }
    
    public String getPackage() { return "origin"; }
}// EventLogicalSubsetter
