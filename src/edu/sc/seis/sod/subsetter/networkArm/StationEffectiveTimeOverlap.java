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
 * specifies the StationEffectiveTimeOverlap
 * <pre>
 *	&lt;stationEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *			&lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/stationEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *      &lt;stationEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/stationEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *	&lt;stationEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *			&lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/stationEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *	&lt;stationEffectiveTimeOverlap&gt;
 *		&lt;effectiveTimeOverlap&gt;
 *		&lt;/effectiveTimeOverlap&gt;
 *	&lt;/stationEffectiveTimeOverlap&gt;
 * </pre>
 */



public class StationEffectiveTimeOverlap extends
EffectiveTimeOverlap implements StationSubsetter {
    /**
     * Creates a new <code>StationEffectiveTimeOverlap</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public StationEffectiveTimeOverlap (Element config){
	super(config);

    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Station station,  CookieJar cookies) {
	return overlaps(station.effective_time);
    }

    static Category logger = 
        Category.getInstance(StationEffectiveTimeOverlap.class.getName());

}// StationEffectiveTimeOverlap
