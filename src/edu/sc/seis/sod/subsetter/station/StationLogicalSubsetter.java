package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

/**
 * @author groves Created on Aug 30, 2004
 */
public class StationLogicalSubsetter extends LogicalSubsetter {

    public StationLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public String getArmName() {
        return "station";
    }
}