package edu.sc.seis.sod.subsetter.site;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;

public class SiteEffectiveTimeOverlap extends
    EffectiveTimeOverlap implements SiteSubsetter {
    public SiteEffectiveTimeOverlap (Element config){
        super(config);
    }

    public boolean accept(Site site) {
        return overlaps(site.effective_time);
    }

    static Category logger =
        Category.getInstance(SiteEffectiveTimeOverlap.class.getName());
}// SiteEffectiveTimeOverlap
