package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import org.w3c.dom.Element;

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
    public OriginDepthRange (Element config) throws Exception{
        super(config);
    }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        QuantityImpl actualDepth = (QuantityImpl)origin.my_location.depth;
        if(actualDepth.greaterThanEqual(getMinDepth()) && actualDepth.lessThanEqual(getMaxDepth())) {
            return true;
        } else return false;
    }
}// OriginDepthRange
