package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

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
    /**
     * Creates a new <code>SiteDepthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SiteDepthRange (Element config)throws Exception{
	super(config);
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param network an <code>NetworkAccess</code> value
     * @param station an <code>Site</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Site site, CookieJar cookies) {
	QuantityImpl actualDepth = (QuantityImpl)site.my_location.depth;
	if(actualDepth.greaterThanEqual((QuantityImpl)getMinDepth()) && actualDepth.lessThanEqual((QuantityImpl)getMaxDepth())) {
	    return true;
	} else return false;
    }

  
  
}// SiteDepthRange
