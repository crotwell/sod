package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;

import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.UserConfigurationException;

public class LatitudeRange extends RangeSubsetter implements SodElement {

    public LatitudeRange(Element config) throws UserConfigurationException {
        super(config);
        check(min, config.getParentNode().getParentNode().getLocalName());
        check(max, config.getParentNode().getParentNode().getLocalName());
    }
    
    public static void check(double latitude, String reporter) throws UserConfigurationException{
        if(latitude > 90 || latitude < -90){
            throw new UserConfigurationException("Latitudes must be between -90 and 90 in " + reporter + ".");
        }
    }
}
