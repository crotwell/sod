package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalLoaderSubsetter;
import edu.sc.seis.sod.subsetter.SubsetterLoader;

/**
 * @author groves Created on Aug 30, 2004
 */
public class ChannelLogicalSubsetter extends LogicalLoaderSubsetter {

    public ChannelLogicalSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public SubsetterLoader getLoader() {
        return new ChannelSubsetterLoader();
    }
}