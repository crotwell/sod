package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.RangeSubsetter;
import org.w3c.dom.Element;


public class OrientationDipRange extends RangeSubsetter implements
    ChannelSubsetter {

    public OrientationDipRange (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel e) throws Exception{
        if(e.an_orientation.dip >= getMinValue() && e.an_orientation.dip <= getMaxValue()) {
            return true;
        } else return false;
    }
}// OrientationDipRange
