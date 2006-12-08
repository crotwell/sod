/**
 * StationPointAzimuth.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.subsetter.station;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.origin.AbstractOriginPoint;

public class StationPointAzimuth  extends AbstractOriginPoint implements StationSubsetter {

    public StationPointAzimuth(Element config) throws Exception{
        super(config);
    }

    /**
     * Accepts an origin only if it lies within the geven azimuth range of the
     * given lat and lon.
     *
     */
    public StringTree accept(Station station, NetworkAccess network) {
        double oLat = station.my_location.latitude;
        double oLon = station.my_location.longitude;
        DistAz distaz = new DistAz(oLat, oLon, latitude, longitude);
        if (getMin().convertTo(UnitImpl.DEGREE).get_value() <= distaz.getAz() &&
            getMax().convertTo(UnitImpl.DEGREE).get_value() >= distaz.getAz()) {
            return new Pass(this);
        } else {
            logger.debug("reject azimuth "+station+" distaz="+distaz.getAz());
            return new Fail(this);
        }
    }

    private static final Logger logger = Logger.getLogger(StationPointAzimuth.class);

}

