package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;


/**
 * sample xml
 *<pre>
 *  &lt;eventStationDistance&gt;
 *        &lt;distanceRange&gt;
 *              &lt;unit&gt;DEGREE&lt;/unit&gt;
 *              &lt;min&gt;30&lt;/min&gt;
 *        &lt;/distanceRange&gt;
 *  &lt;/eventStationDistance&gt;
 *</pre>
 */


public class DistanceRange extends DistanceRangeSubsetter implements EventStationSubsetter {
    public DistanceRange (Element config) throws ConfigurationException{
        super(config);
    }

    public boolean accept(EventAccessOperations eventAccess,  Station station, CookieJar cookieJar)
        throws Exception  {
        Origin origin = null;
        origin = eventAccess.get_preferred_origin();
        double actualDistance = SphericalCoords.distance(origin.my_location.latitude,
                                                         origin.my_location.longitude,
                                                         station.my_location.latitude,
                                                         station.my_location.longitude);
        QuantityImpl dist = new QuantityImpl(actualDistance, UnitImpl.DEGREE);
        if( dist.greaterThanEqual(getMinDistance()) &&
           dist.lessThanEqual(getMaxDistance())) {
            logger.debug("Distance ok "+dist+" from "+getMinDistance()+" "+getMaxDistance());
            return true;

        } else {
            return false;
        }
    }

    private static Logger logger = Logger.getLogger(DistanceRange.class);

}// EventStationDistance
