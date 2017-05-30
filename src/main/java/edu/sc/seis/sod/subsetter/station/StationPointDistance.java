package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
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
    public StringTree accept(StationImpl station, NetworkSource network) {
        Location loc = station.getLocation();
        DistAz distaz = new DistAz(latitude,
                                   longitude,
                                   loc.latitude,
                                   loc.longitude);
        return new StringTreeLeaf(this, min <= distaz.getDelta() && max >= distaz.getDelta());
    }

    private double min, max;
}
