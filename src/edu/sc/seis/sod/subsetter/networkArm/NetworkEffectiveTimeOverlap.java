package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

/**
 * specifies the NetworkEffectiveTimeOverlap
 * <pre>
 *  &lt;networkEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *      &lt;networkEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;networkEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;networkEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/networkEffectiveTimeOverlap&gt;
 * </pre>
 */

public class NetworkEffectiveTimeOverlap extends EffectiveTimeOverlap
    implements NetworkSubsetter {

    public NetworkEffectiveTimeOverlap (Element config){ super(config); }

    public boolean accept(NetworkAttr network) {
        return overlaps(network.effective_time);
    }

    static Category logger =
        Category.getInstance(NetworkEffectiveTimeOverlap.class.getName());
}// NetworkEffectiveTimeOverlap
