package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;

public class StationArea implements StationSubsetter, SodElement {

    public StationArea(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
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
                    && e.my_location.latitude <= boxArea.max_latitude
                    && e.my_location.longitude >= boxArea.min_longitude
                    && e.my_location.longitude <= boxArea.max_longitude) {
                return true;
            } else return false;
        } else if(area instanceof GlobalArea) return true;
        return true;
    }

    private edu.iris.Fissures.Area area = null;
}// StationArea
