package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OriginPointDistance extends AbstractOriginPoint implements
        OriginSubsetter {

    public OriginPointDistance(Element config) throws Exception {
        super(config);
        min = getMin().convertTo(UnitImpl.DEGREE).get_value();
        max = getMax().convertTo(UnitImpl.DEGREE).get_value();
    }

    /**
     * Accepts an origin only if it lies within the geven distance range of the
     * given lat and lon.
     */
    public StringTree accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin origin) {
        double oLat = origin.my_location.latitude;
        double oLon = origin.my_location.longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        double delta = distaz.getDelta();
        return new StringTreeLeaf(this, min <= delta && max >= delta);
    }

    private double min, max;
}