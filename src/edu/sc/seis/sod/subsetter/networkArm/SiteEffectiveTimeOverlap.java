package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;


/**
 * specifies the ChannelEffectiveTimeOverlap
 * <pre>
 *  &lt;siteEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/siteEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *      &lt;siteEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/siteEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;siteEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/siteEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;siteEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/siteEffectiveTimeOverlap&gt;
 * </pre>
 */


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
