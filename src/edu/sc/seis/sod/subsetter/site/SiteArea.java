package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SiteArea.java Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version This class is used to represent the subsetter SiteArea. Site Area
 *          implements SiteSubsetter and can be any one of GlobalArea or BoxArea
 *          or PointDistanceArea or FlinneEngdahlArea.
 */
public class SiteArea implements SiteSubsetter, SodElement {

    public SiteArea(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                area = (edu.iris.Fissures.Area)SodUtil.load((Element)node, "");
                break;
            }
        }
    }

    public boolean accept(Site e) {
        if(area instanceof edu.iris.Fissures.BoxArea) {
            edu.iris.Fissures.BoxArea boxArea = (edu.iris.Fissures.BoxArea)area;
            if(e.my_location.latitude >= boxArea.min_latitude
                    && e.my_location.latitude <= boxArea.max_latitude
                    && e.my_location.longitude >= boxArea.min_longitude
                    && e.my_location.longitude <= boxArea.max_longitude) {
                return true;
            } else return false;
        } else if(area instanceof GlobalArea) return true;
        return true;
    }

    private edu.iris.Fissures.Area area = null;
}// SiteArea
