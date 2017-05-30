package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AzimuthUtils;

public class OriginPointAzimuth extends AbstractOriginPoint implements OriginSubsetter {

    public OriginPointAzimuth(Element config) throws Exception {
        super(config);
        min = getMin().convertTo(UnitImpl.DEGREE).get_value();
        max = getMax().convertTo(UnitImpl.DEGREE).get_value();
    }

    public StringTree accept(CacheEvent event, EventAttrImpl eventAttr, OriginImpl origin) {
        double oLat = origin.getLocation().latitude;
        double oLon = origin.getLocation().longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        if(AzimuthUtils.isAzimuthBetween(distaz, min, max)) {
            return new StringTreeLeaf(this, true);
        } else {
            return new Fail(this, "reject azimuth az=" + distaz.getAz() + "  " + min + " " + max);
        }
    }

    private double min, max;
}
