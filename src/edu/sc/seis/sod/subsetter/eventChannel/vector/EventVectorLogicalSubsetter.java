package edu.sc.seis.sod.subsetter.eventChannel.vector;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class EventVectorLogicalSubsetter extends LogicalSubsetter {

    public EventVectorLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public String getPackage() {
        return "eventChannel.vector";
    }
}