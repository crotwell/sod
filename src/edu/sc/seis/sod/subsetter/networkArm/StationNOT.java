package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * stationNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;stationNOT&gt;
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
 *&lt;/stationNOT&gt;
 * </bold></pre></body>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class StationNOT extends  NetworkLogicalSubsetter
    implements StationSubsetter {

    public StationNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Station e) throws Exception{
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            StationSubsetter filter = (StationSubsetter)it.next();
            if ( filter.accept(e)) { return false; }
        }
        return true;
    }
}// StationNOT
