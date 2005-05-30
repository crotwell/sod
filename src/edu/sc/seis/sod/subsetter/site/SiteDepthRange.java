package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.model.QuantityImpl;
import org.w3c.dom.Element;

public class SiteDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements SiteSubsetter {

    public SiteDepthRange(Element config) throws Exception {
        super(config);
    }

    public boolean accept(Site site) {
        QuantityImpl actualDepth = (QuantityImpl)site.my_location.depth;
        return actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth());
    }
}// SiteDepthRange
