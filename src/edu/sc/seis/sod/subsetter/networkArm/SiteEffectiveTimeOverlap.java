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
 *	&lt;siteEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *			&lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/siteEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *      &lt;siteEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/siteEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *	&lt;siteEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/siteEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *	&lt;siteEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/siteEffectiveTimeOverlap&gt;
 * </pre>
 */


public class SiteEffectiveTimeOverlap extends
EffectiveTimeOverlap implements SiteSubsetter {
    /**
     * Creates a new <code>SiteEffectiveTimeOverlap</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SiteEffectiveTimeOverlap (Element config){
	super(config);

    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Site</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Site site,  CookieJar cookies) {
	return overlaps(site.effective_time);

    }

    static Category logger = 
        Category.getInstance(SiteEffectiveTimeOverlap.class.getName());

}// SiteEffectiveTimeOverlap
