package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class StationSubsetterWrapper implements SiteSubsetter {

    public StationSubsetterWrapper(StationSubsetter sub) {
        this.sub = sub;
    }

    public boolean accept(Site site) throws Exception {
        return sub.accept(site.my_station, null);
    }

    private StationSubsetter sub;
}