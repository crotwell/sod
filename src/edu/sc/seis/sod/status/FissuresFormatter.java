/**
 * FissuresFormatter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.status;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.event.MagnitudeUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;

/**
 * this class largely exists as an access for various utility methods for
 * Velocity templates.
 */
public class FissuresFormatter {

    public static String formatQuantity(QuantityImpl q) {
        return UnitDisplayUtil.formatQuantityImpl(q);
    }

    public static String formatMagnitude(Magnitude m) {
        return MagnitudeUtil.toString(m);
    }

    public static String formatChannel(ChannelId id) {
        if(id == null) { return "null"; }
        return ChannelIdUtil.toStringNoDates(id);
    }

    public static String formatSite(SiteId id) {
        return SiteIdUtil.toStringNoDates(id);
    }

    public static String formatStation(StationId id) {
        return StationIdUtil.toStringNoDates(id);
    }

    public static String formatNetwork(StationId id) {
        return NetworkIdUtil.toStringNoDates(id.network_id);
    }

    public static String formatNetwork(NetworkId id) {
        return NetworkIdUtil.toStringNoDates(id);
    }

    public static String formatNetworkYear(NetworkId id) {
        return id.network_code
                + yearDateFormat.format(new MicroSecondDate(id.begin_time));
    }

    public static String networkName(NetworkAccess net) {
        return networkName(net.get_attributes());
    }

    public static String networkName(NetworkAttr net) {
        return net.name;
    }

    public static String stationName(Station station) {
        return station.name;
    }

    public static QuantityImpl getDepth(Origin origin) {
        return getDepth(origin.my_location);
    }

    public static QuantityImpl getDepth(Location loc) {
        return QuantityImpl.createQuantityImpl(loc.depth);
    }

    public static QuantityImpl getElevation(Location loc) {
        return QuantityImpl.createQuantityImpl(loc.elevation);
    }

    public static float getLatitude(Station station) {
        return getLatitude(station.my_location);
    }

    public static float getLatitude(Origin origin) {
        return getLatitude(origin.my_location);
    }

    public static float getLatitude(Location loc) {
        return loc.latitude;
    }

    public static float getLongitude(Station station) {
        return getLongitude(station.my_location);
    }

    public static float getLongitude(Origin origin) {
        return getLongitude(origin.my_location);
    }

    public static float getLongitude(Location loc) {
        return loc.longitude;
    }

    public static boolean isNull(Object obj) {
        if(obj == null) return true;
        return false;
    }

    public static MicroSecondDate getEffectiveBegin(Station station) {
        return new MicroSecondDate(station.effective_time.start_time);
    }

    public static MicroSecondDate getEffectiveEnd(Station station) {
        return new MicroSecondDate(station.effective_time.end_time);
    }

    public static MicroSecondDate getRangeBegin(TimeRange range) {
        return new MicroSecondDate(range.start_time);
    }

    public static MicroSecondDate getRangeEnd(TimeRange range) {
        return new MicroSecondDate(range.end_time);
    }

    public static QuantityImpl getDistance(ArrayList list) {
        return getDistance((Station)list.get(0), (Origin)list.get(1));
    }

    public static QuantityImpl getDistance(Station station, Origin origin) {
        if(station == null) { throw new NullPointerException("station is null"); }
        if(origin == null) { throw new NullPointerException("origin is null"); }
        if(origin.my_location == null) { throw new NullPointerException("origin.my_location is null"); }
        return getDistance(station.my_location, origin.my_location);
    }

    public static QuantityImpl getDistance(Location from, Location to) {
        if(from == null) { throw new NullPointerException("from Location is null"); }
        if(to == null) { throw new NullPointerException("to Location is null"); }
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getDelta(), UnitImpl.DEGREE);
    }

    public static QuantityImpl getAzimuth(Station station, Origin origin) {
        return getAzimuth(station.my_location, origin.my_location);
    }

    public static QuantityImpl getAzimuth(Location from, Location to) {
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getAz(), UnitImpl.DEGREE);
    }

    public static QuantityImpl getBackAzimuth(Station station, Origin origin) {
        return getBackAzimuth(station.my_location, origin.my_location);
    }

    public static QuantityImpl getBackAzimuth(Location from, Location to) {
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getBaz(), UnitImpl.DEGREE);
    }

    public static String formatDate(Date d) {
        return longFormat.format(d);
    }

    public static String formatDate(Time t) {
        return formatDate(new MicroSecondDate(t));
    }

    public static String formatDateForFile(Date d) {
        return longFileFormat.format(d);
    }

    public static String formatDateForFile(Time t) {
        return formatDateForFile(new MicroSecondDate(t));
    }

    public static String formatDateForFile(Origin origin) {
        return formatDateForFile(new MicroSecondDate(origin.origin_time));
    }

    public static MicroSecondDate now() {
        return ClockUtil.now();
    }

    public static String filize(String fileName) {
        fileName = fileName.trim();
        fileName = fileName.replaceAll("\r?\n *", "");
        fileName = fileName.replaceAll(" *\r?\n", "");
        fileName = fileName.replaceAll("[ :]", "_");
        fileName = fileName.replaceAll("[\t\f]", "");
        return fileName.trim();
    }

    public static String filize(String base, String extension) {
        return filize(base + "." + extension);
    }

    public static SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");

    public static SimpleDateFormat longFileFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");

    public static SimpleDateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

    public static SimpleDateFormat mediumFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    static {
        yearDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        longFileFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        longFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static ParseRegions pr = ParseRegions.getInstance();
}