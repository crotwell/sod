package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.UserConfigurationException;

public class LongitudeRange extends RangeSubsetter implements SodElement {

    public LongitudeRange(Element config) throws UserConfigurationException{
        super(config);
        min = sanitize(min, config.getParentNode().getParentNode().getLocalName());
        max = sanitize(max, config.getParentNode().getParentNode().getLocalName());
    }
    
    public static double sanitize(double longitude, String reporter) throws UserConfigurationException{
        if(longitude > 360 || longitude < -180){
            throw new UserConfigurationException("Longitudes must be between -180 and 360 in " + reporter + ".");
        }
        return BoxAreaImpl.sanitize(longitude);
    }
}
