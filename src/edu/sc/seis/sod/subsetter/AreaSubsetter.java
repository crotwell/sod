package edu.sc.seis.sod.subsetter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.Location;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
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
                locationArray = AreaUtil.loadPolygon(makeRelativeOrRecipeDirReader(fileLocation));
            } catch(FileNotFoundException e) {
                throw new UserConfigurationException(e.getMessage()
                        + " as a polygon file.");
            } catch(IOException e) {
                throw new ConfigurationException("Problem reading from file "
                        + fileLocation, e);
            }
        }
    }

    public static BufferedReader makeRelativeOrRecipeDirReader(String fileLocation)
            throws FileNotFoundException {
        File simpleLocation = new File(fileLocation);
        Reader fileInput;
        try {
            fileInput = new FileReader(simpleLocation);
        } catch(FileNotFoundException e) {
            File inConfigDir = new File(new File(Start.getConfigFileName()).getParentFile(),
                                        fileLocation);
            try {
                fileInput = new FileReader(inConfigDir);
            } catch(FileNotFoundException e2) {
                throw new FileNotFoundException("Unable to find '"
                        + simpleLocation + "' or '" + inConfigDir + "'");
            }
        }
        return new BufferedReader(fileInput);
    }

    public boolean accept(Location loc) {
        if(locationArray != null) {
            return AreaUtil.inArea(locationArray, loc);
        }
        return AreaUtil.inArea(area, loc);
    }

    protected edu.iris.Fissures.Area area = null;

    protected Location[] locationArray;
}
