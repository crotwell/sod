package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

   /**
     * This subsetter specifies the depthRange for the origin
     * 
     *<pre>
     *  &lt;originDepthRange&gt;
     *      &lt;unitRange&gt;
     *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
     *           &lt;min&gt;10&lt;/min&gt;
     *           &lt;max&gt;100&lt;/max&gt;
     *      &lt;/unitRange&gt;
     *  &lt;/originDepthRange&gt;
     * 
     *                  (or)
     *
     *  &lt;originDepthRange&gt;
     *      &lt;unitRange&gt;
     *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
     *           &lt;min&gt;10&lt;/min&gt;
     *      &lt;/unitRange&gt;
     *  &lt;/originDepthRange&gt;
     *
     *                  (or)
     *
     *  &lt;originDepthRange&gt;
     *      &lt;unitRange&gt;
     *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
     *           &lt;max&gt;100&lt;/max&gt;
     *      &lt;/unitRange&gt;
     *  &lt;/originDepthRange&gt;
     * 
     *                  (or)
     *
     *  &lt;originDepthRange&gt;
     *      &lt;unitRange&gt;
     *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
     *      &lt;/unitRange&gt;
     *  &lt;/originDepthRange&gt;
     *</pre>
     */

public class OriginDepthRange extends edu.sc.seis.sod.subsetter.DepthRange implements OriginSubsetter{
 
    
    /**
     * Creates a new <code>OriginDepthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public OriginDepthRange (Element config) throws Exception{
	super(config);
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event, Origin origin, CookieJar cookies) {
	QuantityImpl actualDepth = (QuantityImpl)origin.my_location.depth;
	if(actualDepth.greaterThanEqual((QuantityImpl)getMinDepth()) && actualDepth.lessThanEqual((QuantityImpl)getMaxDepth())) {
	    return true;
	} else return false;

    }

  
}// OriginDepthRange
