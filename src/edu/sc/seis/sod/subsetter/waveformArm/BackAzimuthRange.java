package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;

public class BackAzimuthRange extends RangeSubsetter implements EventStationSubsetter {
    public BackAzimuthRange (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations eventAccess,  Station station, CookieJar cookieJar)
        throws Exception{
        float minValue = getMinValue();
        float maxValue = getMaxValue();
        if(minValue > 180) minValue = minValue - 360;
        if(maxValue > 180) maxValue = maxValue - 360;
        Origin origin = eventAccess.get_preferred_origin();
        double azimuth = SphericalCoords.azimuth(station.my_location.latitude,
                                                 station.my_location.longitude,
                                                 origin.my_location.latitude,
                                                 origin.my_location.longitude);

        if(azimuth >= minValue && azimuth <= maxValue) {
            return true;
        } else return false;
    }

}// BackAzimuthRange
