/**
 * OriginPointDistance.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OriginPointDistance extends edu.sc.seis.sod.subsetter.DistanceRangeSubsetter implements OriginSubsetter{


    /**
     * Creates a new <code>OriginPointDistance</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public OriginPointDistance (Element config) throws Exception{
        super(config);
        NodeList nodeList = config.getElementsByTagName("latitude");
        latitude = Double.parseDouble(XMLUtil.getText((Element)nodeList.item(0)));
        nodeList = config.getElementsByTagName("longitude");
        longitude = Double.parseDouble(XMLUtil.getText((Element)nodeList.item(0)));
    }

    /**
     * Accepts an origin only if it lies within the geven distance range of the
     * given lat and lon.
     *
     */
    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        double oLat = origin.my_location.latitude;
        double oLon = origin.my_location.longitude;
        DistAz distaz = new DistAz(latitude, longitude, oLat, oLon);
        if (getMinDistance().convertTo(UnitImpl.DEGREE).get_value() <= distaz.delta &&
            getMaxDistance().convertTo(UnitImpl.DEGREE).get_value() >= distaz.delta) {
            return true;
        } else {
            return false;
        }
    }

    double latitude = 0.0;
    double longitude = 0.0;
}

