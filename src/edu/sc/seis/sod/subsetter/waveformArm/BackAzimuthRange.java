package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.AzimuthUtils;
import edu.sc.seis.sod.subsetter.RangeSubsetter;
import org.w3c.dom.Element;

public class BackAzimuthRange extends RangeSubsetter implements EventStationSubsetter {
    public BackAzimuthRange (Element config) throws ConfigurationException {
        super(config);
        min = getMinValue();
        max = getMaxValue();
    }

    public boolean accept(EventAccessOperations ev,  Station sta, CookieJar cookieJar){
        return AzimuthUtils.isBackAzimuthBetween(new DistAz(sta, ev), min, max);
    }

    private float min, max;
}// BackAzimuthRange
