/**
 * OriginPointDistance.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;


import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.fissuresUtil.bag.DistAz;

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
     * @param event an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event,
                          Origin origin,
                          CookieJar cookies) {
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

