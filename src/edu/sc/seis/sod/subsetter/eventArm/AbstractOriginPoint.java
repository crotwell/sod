/**
 * AbstractOriginPoint.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AbstractOriginPoint extends DistanceRangeSubsetter {

    public AbstractOriginPoint (Element config) throws Exception{
        super(config);
        NodeList nodeList = config.getElementsByTagName("latitude");
        latitude = Double.parseDouble(XMLUtil.getText((Element)nodeList.item(0)));
        nodeList = config.getElementsByTagName("longitude");
        longitude = Double.parseDouble(XMLUtil.getText((Element)nodeList.item(0)));
    }

    protected double latitude = 0.0;

    protected double longitude = 0.0;
}

