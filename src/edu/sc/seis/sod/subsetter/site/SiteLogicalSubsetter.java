package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalLoaderSubsetter;
import edu.sc.seis.sod.subsetter.SubsetterLoader;

/**
 * @author groves Created on Aug 30, 2004
 */
public class SiteLogicalSubsetter extends LogicalLoaderSubsetter {

    public SiteLogicalSubsetter(Element config) throws ConfigurationException {
        super(config);
    }

    public SubsetterLoader getLoader() {
        return new SiteSubsetterLoader();
    }
}