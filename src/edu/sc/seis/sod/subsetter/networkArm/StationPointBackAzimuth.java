/**
 * StationPointBackAzimuth.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.sod.subsetter.eventArm.AbstractOriginPoint;
import edu.sc.seis.sod.subsetter.networkArm.StationPointDistance;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class StationPointBackAzimuth   extends AbstractOriginPoint implements StationSubsetter {

    public StationPointBackAzimuth(Element config) throws Exception{
        super(config);
    }

    /**
     * Accepts an origin only if it lies within the geven azimuth range of the
     * given lat and lon.
     *
     */
    public boolean accept(Station station) {
        double oLat = station.my_location.latitude;
        double oLon = station.my_location.longitude;
        DistAz distaz = new DistAz(oLat, oLon, latitude, longitude);
        if (getMin().convertTo(UnitImpl.DEGREE).get_value() <= distaz.getBaz() &&
            getMax().convertTo(UnitImpl.DEGREE).get_value() >= distaz.getBaz()) {
            return true;
        } else {
            logger.debug("reject back azimuth "+station+" distaz="+distaz.getBaz());
            return false;
        }
    }

    private static final Logger logger = Logger.getLogger(StationPointBackAzimuth.class);

}

