package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.RangeSubsetter;
import org.w3c.dom.Element;

public class OrientationAzimuthRange extends RangeSubsetter
    implements ChannelSubsetter {

    public OrientationAzimuthRange(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel e) throws Exception{
        float minValue = getMinValue();
        float maxValue = getMaxValue();
        if(minValue > 180) minValue = minValue - 360;
        if(maxValue > 180) maxValue = maxValue - 360;
        if(e.an_orientation.azimuth >= minValue &&
           e.an_orientation.azimuth <= maxValue) {
            return true;
        } else return false;
    }
}// OrientationAzimuthRange
