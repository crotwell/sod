package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.model.QuantityImpl;
import org.w3c.dom.Element;

/**
 * This subsetter specifies the depthRange for the origin
 * <pre>
 *
 *  &lt;siteDepthRange&gt;
 *      &lt;unitRange&gt;
 *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *           &lt;min&gt;10&lt;/min&gt;
 *           &lt;max&gt;100&lt;/max&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/siteDepthRange&gt;
 *
 *                  (or)
 *
 *  &lt;siteDepthRange&gt;
 *      &lt;unitRange&gt;
 *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *           &lt;min&gt;10&lt;/min&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/siteDepthRange&gt;
 *
 *                  (or)
 *
 *  &lt;siteDepthRange&gt;
 *      &lt;unitRange&gt;
 *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *           &lt;max&gt;100&lt;/max&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/siteDepthRange&gt;
 *
 *                  (or)
 *
 *  &lt;siteDepthRange&gt;
 *      &lt;unitRange&gt;
 *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/siteDepthRange&gt;
 * </pre>
 */

public class SiteDepthRange extends edu.sc.seis.sod.subsetter.DepthRange implements SiteSubsetter{
    public SiteDepthRange (Element config)throws Exception{
        super(config);
    }

    public boolean accept(Site site) {
        QuantityImpl actualDepth = (QuantityImpl)site.my_location.depth;
        if(actualDepth.greaterThanEqual(getMinDepth()) && actualDepth.lessThanEqual(getMaxDepth())) {
            return true;
        } else return false;
    }
}// SiteDepthRange
