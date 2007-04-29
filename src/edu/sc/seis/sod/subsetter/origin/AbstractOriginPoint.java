/**
 * AbstractOriginPoint.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;
import edu.sc.seis.sod.subsetter.LatitudeRange;
import edu.sc.seis.sod.subsetter.LongitudeRange;

public class AbstractOriginPoint extends DistanceRangeSubsetter {

    public AbstractOriginPoint(Element config) throws Exception {
        super(config);
        double[] out = getLatLon(config, config.getNodeName());
        latitude = out[0];
        longitude = out[1];
    }

    public static double[] getLatLon(Element config, String extractor) throws UserConfigurationException {
        double[] out = {DOMHelper.extractDouble(config, "latitude", 0.0),
                        DOMHelper.extractDouble(config, "longitude", 0.0)};
        LatitudeRange.check(out[0], extractor);
        out[1] = LongitudeRange.sanitize(out[1], extractor);
        return out;
    }

    protected double latitude = 0.0;

    protected double longitude = 0.0;
}
