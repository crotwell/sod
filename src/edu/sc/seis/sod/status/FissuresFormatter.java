/**
 * FissuresFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.event.MagnitudeUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;

/** this class largely exists as an access for various utility methods for
 * Velocity templates.*/
public class FissuresFormatter {
    public static String formatQuantity(QuantityImpl q) {
        return UnitDisplayUtil.formatQuantityImpl(q);
    }

    public static String formatMagnitude(Magnitude m) {
        return MagnitudeUtil.toString(m);
    }

    public static String formatChannel(ChannelId id) {
        return ChannelIdUtil.toStringNoDates(id);
    }

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

    public static MicroSecondDate getRangeBegin(TimeRange range) {
        return new MicroSecondDate(range.start_time);
    }

    public static MicroSecondDate getRangeEnd(TimeRange range) {
        return new MicroSecondDate(range.end_time);
    }

}


