package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AzimuthUtils;

public class OriginPointBackAzimuth extends AbstractOriginPoint implements OriginSubsetter {

    public OriginPointBackAzimuth(Element config) throws Exception {
        super(config);
        min = getMin().convertTo(UnitImpl.DEGREE).get_value();
        max = getMax().convertTo(UnitImpl.DEGREE).get_value();
    }

    public StringTree accept(CacheEvent event, EventAttr eventAttr, Origin origin) {
        double oLat = origin.getLocation().latitude;
        double oLon = origin.getLocation().longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        if(AzimuthUtils.isBackAzimuthBetween(distaz, min, max)) {
            return new StringTreeLeaf(this, true);
        } else {
            return new Fail(this, distaz.getBaz() + " not between  " + min + ", " + max);
        }
    }

    private double min, max;
}
