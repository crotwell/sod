package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.subsetter.site.SiteSubsetter;

public class SiteSubsetterExample implements SiteSubsetter {

    public boolean accept(Site site, NetworkAccess network) throws Exception {
        return false;
    }
}
