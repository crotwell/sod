package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

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
public final class StationOR
    extends NetworkLogicalSubsetter
    implements StationSubsetter {

    /**
     * Creates a new <code>StationOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public StationOR (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAccess  network,  Station e,  CookieJar cookies) throws Exception{
    Iterator it = filterList.iterator();
    while(it.hasNext()) {
        StationSubsetter filter = (StationSubsetter)it.next();
        if ( filter.accept(network, e, cookies)) {
        return true;
        }
    }
    return false;
    }

}// StationOR
