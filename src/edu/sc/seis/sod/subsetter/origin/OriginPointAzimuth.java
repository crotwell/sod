package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
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

    public StringTree accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        double oLat = origin.my_location.latitude;
        double oLon = origin.my_location.longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        if(AzimuthUtils.isAzimuthBetween(distaz, min, max)) {
            return new StringTreeLeaf(this, true);
        } else {
            return new Fail(this, "reject azimuth az=" + distaz.getAz() + "  " + min + " " + max);
        }
    }

    private double min, max;
}
