package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * StationArea.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 *
 * This class is used to represent the subsetter StationArea. Station Area implements StationSubsetter
 * and can be any one of GlobalArea or BoxArea or PointDistanceArea or FlinneEngdahlArea.
 *
 * sample xml representation of StationArea are
 *
 * <pre>
 *
 *              &lt;stationArea&gt;
 *                           &lt;boxArea&gt;
 *                                    &lt;latitudeRange&gt;
 *                                                   &lt;min&gt;30&lt;/min&gt;
 *                                                   &lt;max&gt;33&lt;/max&gt;
 *                                    &lt;/latitudeRange&gt;
 *                                    &lt;longitudeRange&gt;
 *                                                   &lt;min&gt;-100&lt;/min&gt;
 *                                                   &lt;max&gt;100&lt;/max&gt;
 *                                    &lt;/longitudeRange&gt;
 *                           &lt;/boxArea&gt;
 *              &lt;/stationArea&gt;
 * </pre>
 */


public class StationArea implements StationSubsetter,SodElement {
    public StationArea (Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength() ; i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                area = (edu.iris.Fissures.Area)SodUtil.load((Element)node, "");
                break;
            }
        }
    }

    public boolean accept(Station e) {
        if(area instanceof edu.iris.Fissures.BoxArea) {
            edu.iris.Fissures.BoxArea boxArea = (edu.iris.Fissures.BoxArea)area;
            if(e.my_location.latitude >= boxArea.min_latitude
               && e.my_location.latitude <=boxArea.max_latitude
               && e.my_location.longitude >= boxArea.min_longitude
               && e.my_location.longitude <= boxArea.max_longitude) {
                return true;
            } else return false;
        } else if(area instanceof GlobalArea) return true;
        return true;

    }

    private edu.iris.Fissures.Area area = null;
}// StationArea
