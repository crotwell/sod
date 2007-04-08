/**
 * AbstractOriginPoint.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;

public class AbstractOriginPoint extends DistanceRangeSubsetter {

    public AbstractOriginPoint(Element config) throws Exception {
        super(config);
        double[] out = getLatLon(config);
        latitude = out[0];
        longitude = out[1];
    }

    public static double[] getLatLon(Element config) {
        double[] out = {DOMHelper.extractDouble(config, "latitude", 0.0),
                        DOMHelper.extractDouble(config, "longitude", 0.0)};
        out[1] = BoxAreaImpl.sanitize(out[1]);
        return out;
    }

    protected double latitude = 0.0;

    protected double longitude = 0.0;
}
