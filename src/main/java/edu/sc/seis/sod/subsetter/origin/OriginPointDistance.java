package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.common.Area;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.PointDistanceAreaImpl;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OriginPointDistance extends AbstractOriginPoint implements OriginSubsetter {

    public OriginPointDistance(Element config) throws Exception {
        super(config);
        min = getMin().convertTo(UnitImpl.DEGREE).get_value();
        max = getMax().convertTo(UnitImpl.DEGREE).get_value();
    }

    /**
     * Accepts an origin only if it lies within the given distance range of the
     * given lat and lon.
     */
    public StringTree accept(CacheEvent event, EventAttrImpl eventAttr, OriginImpl origin) {
        double oLat = origin.getLocation().latitude;
        double oLon = origin.getLocation().longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        double delta = distaz.getDelta();
        return new StringTreeLeaf(this, min <= delta && max >= delta);
    }

    private double min, max;

    public Area getArea() {
        return new PointDistanceAreaImpl((float)latitude,
                                         (float)longitude,
                                         new QuantityImpl(min, UnitImpl.DEGREE),
                                         new QuantityImpl(max, UnitImpl.DEGREE));
    }
}