package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.subsetter.EffectiveTimeOverlap;
import org.apache.log4j.Category;
import org.w3c.dom.Element;


/**
 * specifies the StationEffectiveTimeOverlap
 * <pre>
 *  &lt;stationEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/stationEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *      &lt;stationEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;max&gt;2003-01-01T00:00:00Z&lt;/max&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/stationEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;stationEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *          &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/stationEffectiveTimeOverlap&gt;
 *
 *                    (or)
 *
 *  &lt;stationEffectiveTimeOverlap&gt;
 *      &lt;effectiveTimeOverlap&gt;
 *      &lt;/effectiveTimeOverlap&gt;
 *  &lt;/stationEffectiveTimeOverlap&gt;
 * </pre>
 */
public class StationEffectiveTimeOverlap extends EffectiveTimeOverlap
    implements StationSubsetter {

    public StationEffectiveTimeOverlap (Element config){ super(config); }

    public boolean accept(Station station) {
        return overlaps(station.effective_time);
    }

    static Category logger =
        Category.getInstance(StationEffectiveTimeOverlap.class.getName());
}// StationEffectiveTimeOverlap
