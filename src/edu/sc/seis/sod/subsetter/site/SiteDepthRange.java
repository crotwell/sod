package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.model.QuantityImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class SiteDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements SiteSubsetter {

    public SiteDepthRange(Element config) throws Exception {
        super(config);
    }

    public StringTree accept(Site site, NetworkAccess network) {
        QuantityImpl actualDepth = (QuantityImpl)site.my_location.depth;
        return new StringTreeLeaf(this, actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth()));
    }
}// SiteDepthRange
