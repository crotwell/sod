package edu.sc.seis.sod.subsetter.site;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;

public class SiteEffectiveTimeOverlap extends EffectiveTimeOverlap implements
        SiteSubsetter {

    public SiteEffectiveTimeOverlap(Element config)
            throws ConfigurationException {
        super(config);
    }

    public SiteEffectiveTimeOverlap(TimeRange tr) {
        super(tr);
    }

    public boolean accept(Site site, NetworkAccess network) {
        return overlaps(site.effective_time);
    }

    static Category logger = Category.getInstance(SiteEffectiveTimeOverlap.class.getName());
}// SiteEffectiveTimeOverlap
