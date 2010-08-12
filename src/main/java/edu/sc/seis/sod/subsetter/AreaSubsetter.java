package edu.sc.seis.sod.subsetter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

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
            locationArray = extractPolygon(fileLocation);
        }
    }

    public static Location[] extractPolygon(String fileLocation)
            throws ConfigurationException {
        Location[] locs;
        try {
            locs = AreaUtil.loadPolygon(makeRelativeOrRecipeDirReader(fileLocation));
        } catch(FileNotFoundException e) {
            throw new UserConfigurationException(e.getMessage()
                    + " as a polygon file.");
        } catch(IOException e) {
            throw new ConfigurationException("Problem reading from file "
                    + fileLocation, e);
        }
        for(int i = 0; i < locs.length; i++) {
            LatitudeRange.check(locs[i].latitude, fileLocation);
            LongitudeRange.sanitize(locs[i].longitude, fileLocation);
        }
        return locs;
    }

    public static BufferedReader makeRelativeOrRecipeDirReader(String fileLocation)
            throws FileNotFoundException {
        try {
            URI uri = new URI(fileLocation);
            return new BufferedReader(new InputStreamReader(uri.toURL().openStream()));
        } catch(URISyntaxException e1) {
            // not a uri, try as simple file
        } catch(IOException e) {
            // not a uri, try as simple file
        }
        File simpleLocation = new File(fileLocation.trim());
        Reader fileInput;
        try {
            fileInput = new FileReader(simpleLocation);
        } catch(FileNotFoundException e) {
            File inConfigDir = new File(new File(Start.getConfigFileName()).getParentFile(),
                                        fileLocation);
            try {
                fileInput = new FileReader(inConfigDir);
            } catch(FileNotFoundException e2) {
                throw new FileNotFoundException("Unable to find as URL or '"
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
