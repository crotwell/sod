package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Location;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.RangeSubsetter;
import org.w3c.dom.Element;


/**
 * specifies the azimuth Range
 *<pre>
 * &lt;azimuthRange&gt;
 *      &lt;min&gt;30&lt;/min&gt;
 *      &lt;max&gt;180&lt;/max&gt;
 * &lt;/azimuthRange&gt;
 *</pre>
 */

public class AzimuthRange extends RangeSubsetter implements EventChannelSubsetter {
    public AzimuthRange (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations eventAccess,  NetworkAccess network,Channel chan, CookieJar cookies) throws Exception {
        float minValue = getMinValue();
        float maxValue = getMaxValue();
        if(minValue > 180) minValue = minValue - 360;
        if(maxValue > 180) maxValue = maxValue - 360;
        Origin origin = eventAccess.get_preferred_origin();
        Location originLoc = origin.my_location;
        Location loc = chan.my_site.my_location;
        double azimuth = SphericalCoords.azimuth(originLoc.latitude,
                                                 originLoc.longitude,
                                                 loc.latitude,
                                                 loc.longitude);

        if(azimuth >= minValue && azimuth <= maxValue) return true;
        else return false;
    }
}// AzimuthRange
