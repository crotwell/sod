package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.Orientation;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;


public class OrientationRange implements ChannelSubsetter {
    public OrientationRange (Element config){
        azimuth = Float.parseFloat(SodUtil.getNestedText(SodUtil.getElement(config, "azimuth")));
        dip = Float.parseFloat(SodUtil.getNestedText(SodUtil.getElement(config, "dip")));
        offset = Float.parseFloat(SodUtil.getNestedText(SodUtil.getElement(config, "maxOffset")));
    }

    public boolean accept(Channel e) throws Exception{
        Orientation ori = e.an_orientation;
        double actualDistance = SphericalCoords.distance(ori.dip, ori.azimuth,
                                                         dip, azimuth);
        if(actualDistance <= offset) { return true;
        }else {return false;}
    }

    private float azimuth;
    private float dip;
    private float offset;
}// OrientationRange
