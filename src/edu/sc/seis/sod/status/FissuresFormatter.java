/**
 * FissuresFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.event.MagnitudeUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import java.text.SimpleDateFormat;

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

    public static String formatSite(SiteId id) {
        return SiteIdUtil.toStringNoDates(id);
    }

    public static String formatStation(StationId id) {
        return StationIdUtil.toStringNoDates(id);
    }

    public static String formatNetwork(NetworkId id) {
        return NetworkIdUtil.toStringNoDates(id);
    }
    public static String formatNetworkYear(NetworkId id) {
        return NetworkIdUtil.toStringNoDates(id)+yearDateFormat.format(new MicroSecondDate(id.begin_time));
    }

    public static String networkName(NetworkAccess net) {
        return net.get_attributes().name;
    }

    public static String networkCodeYear(NetworkAccess net) {
        return net.get_attributes().get_code()+net.get_attributes().get_id().begin_time.date_time.substring(0, 4);
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

    public static QuantityImpl getDistance(Location from, Location to) {
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getDelta(), UnitImpl.DEGREE);
    }

    public static QuantityImpl getAzimuth(Location from, Location to) {
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getAz(), UnitImpl.DEGREE);
    }

    public static QuantityImpl getBackAzimuth(Location from, Location to) {
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getBaz(), UnitImpl.DEGREE);
    }

    public static SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
}



