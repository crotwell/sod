package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;

/**
 * @author groves Created on Aug 30, 2004
 */
public class ChannelLogicalSubsetter extends LogicalSubsetter {

    public ChannelLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public String getArmName() {
        return "channel";
    }
}