package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.origin.AbstractOriginPoint;

public class StationPointDistance extends AbstractOriginPoint implements
        StationSubsetter {

    public StationPointDistance(Element config) throws Exception {
        super(config);
        min = getMin().convertTo(UnitImpl.DEGREE).get_value();
        max = getMax().convertTo(UnitImpl.DEGREE).get_value();
    }

    /**
     * Accepts a station only if it lies within the given distance range of the
     * given lat and lon.
     */
    public StringTree accept(Station station, NetworkAccess network) {
        Location loc = station.getLocation();
        DistAz distaz = new DistAz(latitude,
                                   longitude,
                                   loc.latitude,
                                   loc.longitude);
        return new StringTreeLeaf(this, min <= distaz.getDelta() && max >= distaz.getDelta());
    }

    private double min, max;
}
