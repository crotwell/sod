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
 * specifies the NetworkEffectiveTimeOverlap
 * <pre>
 *	&lt;networkeffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *			&lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/networkeffectiveTimeOverlap&gt;
 *
 *                    (or)
 *      &lt;networkeffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/networkeffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *	&lt;networkeffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/networkeffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *	&lt;networkeffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/networkeffectiveTimeOverlap&gt;
 * </pre>
 */

public class NetworkeffectiveTimeOverlap extends
EffectiveTimeOverlap implements NetworkAttrSubsetter {
    /**
     * Creates a new <code>NetworkeffectiveTimeOverlap</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public NetworkeffectiveTimeOverlap (Element config){
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e a <code>NetworkAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAttr network,  CookieJar cookies) {
	return overlaps(network.effective_time);
	
    }

    static Category logger = 
        Category.getInstance(NetworkeffectiveTimeOverlap.class.getName());

}// NetworkeffectiveTimeOverlap
