package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * stationOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *
 * sample xml file
 *pre><bold>
 *&lt;stationOR&gt;
 *               &lt;stationArea&gt;
 *          &lt;boxArea&gt;
 *          &lt;latitudeRange&gt;
 *              &lt;min&gt;20&lt;/min&gt;
 *              &lt;max&gt;40&lt;/max&gt;
 *          &lt;/latitudeRange&gt;
 *          &lt;longitudeRange&gt;
 *              &lt;min&gt;-100&lt;/min&gt;
 *              &lt;max&gt;-80&lt;/max&gt;
 *          &lt;/longitudeRange&gt;
 *          &lt;/boxArea&gt;
 *      &lt;/stationArea&gt;
 *      &lt;stationeffectiveTimeOverlap&gt;
 *          &lt;effectiveTimeOverlap&gt;
 *              &lt;min&gt;1999-01-01T00:00:00Z&lt;/min&gt;
 *              &lt;max&gt;2000-01-01T00:00:00Z&lt;/max&gt;
 *          &lt;/effectiveTimeOverlap&gt;
 *      &lt;/stationeffectiveTimeOverlap&gt;
 *&lt;/stationOR&gt;
 * </bold></pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class StationOR extends NetworkLogicalSubsetter
    implements StationSubsetter {

    public StationOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Station e) throws Exception{
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            StationSubsetter filter = (StationSubsetter)it.next();
            if ( filter.accept(e)){ return true; }
        }
        return false;
    }

}// StationOR
