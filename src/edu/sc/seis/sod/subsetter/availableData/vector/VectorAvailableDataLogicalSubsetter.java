package edu.sc.seis.sod.subsetter.availableData.vector;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

/**
 * @author groves Created on Aug 31, 2004
 */
public class VectorAvailableDataLogicalSubsetter extends LogicalSubsetter {

    public VectorAvailableDataLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public String getPackage() {
        return "availableData.vector";
    }
}