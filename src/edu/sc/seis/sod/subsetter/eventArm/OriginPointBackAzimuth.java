/**
 * OriginPointBackAzimuth.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class OriginPointBackAzimuth extends AbstractOriginPoint implements OriginSubsetter{
    public OriginPointBackAzimuth (Element config) throws Exception{
        super(config);
        min = getMin().convertTo(UnitImpl.DEGREE).get_value();
        max = getMax().convertTo(UnitImpl.DEGREE).get_value();
    }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        double oLat = origin.my_location.latitude;
        double oLon = origin.my_location.longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        double baz = distaz.getBaz();
        baz = (baz - min)%360 + min;
        if (min  <= baz && max >= baz) { return true;}
        else {
            logger.debug("reject back azimuth baz="+distaz.getBaz()+"  "+ min +" "+ max);
            return false;
        }
    }

    private double min, max;
    private static final Logger logger = Logger.getLogger(OriginPointBackAzimuth.class);

}

