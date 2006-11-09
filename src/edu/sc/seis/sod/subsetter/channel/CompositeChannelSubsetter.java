package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalLoaderSubsetter;
import edu.sc.seis.sod.subsetter.SubsetterLoader;


public abstract class CompositeChannelSubsetter extends LogicalLoaderSubsetter implements
        ChannelSubsetter {

    public CompositeChannelSubsetter(Element config)
            throws ConfigurationException {
        super(config);
    }

    public SubsetterLoader getLoader() {
        return new ChannelSubsetterLoader();
    }
}
