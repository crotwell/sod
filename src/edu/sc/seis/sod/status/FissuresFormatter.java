/**
 * FissuresFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.event.MagnitudeUtil;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;

/** this class largely exists as an access for various utility methods for
 * Velocity templates.*/
public class FissuresFormatter
{

    public static String formatMagnitude(Magnitude m) { return MagnitudeUtil.toString(m); }

    public static String formatChannel(ChannelId id) { return ChannelIdUtil.toStringNoDates(id); }

    public static QuantityImpl getDepth(Location loc) {
        return QuantityImpl.createQuantityImpl(loc.depth);
    }

    public static QuantityImpl getElevation(Location loc) {
        return QuantityImpl.createQuantityImpl(loc.elevation);
    }

    public static float getLatitude(Location loc) {
        return loc.latitude;
    }

    public static float getLongitude(Location loc) {
        return loc.longitude;
    }

}

