/**
 * StationPointDistance.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.sod.subsetter.eventArm.AbstractOriginPoint;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class StationPointDistance extends AbstractOriginPoint implements StationSubsetter{


    /**
     * Creates a new <code>OriginPointDistance</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public StationPointDistance (Element config) throws Exception{
        super(config);
    }

    /**
     * Accepts an origin only if it lies within the geven distance range of the
     * given lat and lon.
     *
     */
    public boolean accept(Station station) {
        double oLat = station.my_location.latitude;
        double oLon = station.my_location.longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        if (getMin().convertTo(UnitImpl.DEGREE).get_value() <= distaz.getDelta() &&
            getMax().convertTo(UnitImpl.DEGREE).get_value() >= distaz.getDelta()) {
            return true;
        } else {
            logger.debug("reject distance "+station+" distaz="+distaz.getDelta());
            return false;
        }
    }

    private static final Logger logger = Logger.getLogger(StationPointDistance.class);

}

