/**
 * AbstractOriginPoint.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.origin;

import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AbstractOriginPoint extends DistanceRangeSubsetter {

    public AbstractOriginPoint (Element config) throws Exception{
        super(config);
        double[] out = getLatLon(config);
        latitude = out[0];
        longitude = out[1];
    }

    public static double[] getLatLon(Element config) {
        double[] out = new double[2];
        NodeList nodeList = config.getElementsByTagName("latitude");
        out[0] = Double.parseDouble(XMLUtil.getText((Element)nodeList.item(0)));
        nodeList = config.getElementsByTagName("longitude");
        out[1] = Double.parseDouble(XMLUtil.getText((Element)nodeList.item(0)));
        return out;
    }

    protected double latitude = 0.0;

    protected double longitude = 0.0;
}

