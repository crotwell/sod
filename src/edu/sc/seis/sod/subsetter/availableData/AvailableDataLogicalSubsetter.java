package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class AvailableDataLogicalSubsetter extends LogicalSubsetter {

    public AvailableDataLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public String getArmName() {
        return "availableData";
    }
}