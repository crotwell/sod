package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;

/**
 * Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a> This class is used to
 *         represent the subsetter EventArea. Event Area implements
 *         EventSubsetter and can be any one of GlobalArea or BoxArea or
 *         PointDistanceArea or FlinneEngdahlArea.
 */
public class EventArea implements OriginSubsetter, SodElement {

    public EventArea(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                area = (edu.iris.Fissures.Area)SodUtil.load((Element)node,
                                                            "eventArm");
                break;
            }
        }
    }

    /**
     * returns true if the given origin is within the area specified in the
     * configuration file else returns false.
     */
    public boolean accept(EventAccessOperations event,
                          EventAttr eventAttr,
                          Origin e) throws Exception {
        if(area instanceof BoxArea) {
            BoxArea boxArea = (BoxArea)area;
            if(e.my_location.latitude >= boxArea.min_latitude
                    && e.my_location.latitude <= boxArea.max_latitude
                    && e.my_location.longitude >= boxArea.min_longitude
                    && e.my_location.longitude <= boxArea.max_longitude) { return true; }
            return false;
        } else if(area instanceof GlobalArea) { return true; }
        throw new Exception("Unknown Area, class=" + area.getClass());
    }

    private edu.iris.Fissures.Area area = null;
}// EventArea
