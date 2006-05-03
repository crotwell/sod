package edu.sc.seis.sod.subsetter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.Location;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.UserConfigurationException;

/**
 * SiteArea.java Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version This class is used to represent the subsetter SiteArea. Site Area
 *          implements SiteSubsetter and can be any one of GlobalArea or BoxArea
 *          or PointDistanceArea or FlinneEngdahlArea.
 */
public class AreaSubsetter {

    public AreaSubsetter(Element config) throws ConfigurationException {
        String fileLocation = DOMHelper.extractText(config, "polygonFile", "");
        if(fileLocation.equals("")) {
            NodeList children = config.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if(node instanceof Element) {
                    area = (edu.iris.Fissures.Area)SodUtil.load((Element)node,
                                                                "");
                    break;
                }
            }
        } else {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileLocation)));
                locationArray = AreaUtil.loadPolygon(reader);
            } catch(FileNotFoundException e) {
                throw new UserConfigurationException("Unable to find '"
                        + fileLocation + "' to load as a polygon.");
            } catch(IOException e) {
                throw new ConfigurationException("Problem reading from file "
                        + fileLocation, e);
            }
        }
    }

    public boolean accept(Location loc) {
        if(area instanceof BoxArea) {
            return AreaUtil.inArea((BoxArea)area, loc);
        } else if(area instanceof GlobalArea) {
            return true;
        } else if(locationArray != null) {
            return AreaUtil.inArea(locationArray, loc);
        }
        return true;
    }

    protected edu.iris.Fissures.Area area = null;

    protected Location[] locationArray;
}
