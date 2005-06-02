package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;

public final class SiteXOR extends SiteLogicalSubsetter implements
        SiteSubsetter {

    public SiteXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Site site, NetworkAccess network) throws Exception {
        SiteSubsetter filterA = (SiteSubsetter)subsetters.get(0);
        SiteSubsetter filterB = (SiteSubsetter)subsetters.get(1);
        return (filterA.accept(site, null) != filterB.accept(site, network));
    }
}// SiteXOR
