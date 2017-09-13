/**
 * FissuresFormatter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.status;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TimeZone;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.Magnitude;
import edu.sc.seis.sod.model.event.MagnitudeUtil;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.NetworkId;
import edu.sc.seis.sod.model.station.StationId;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.util.display.ChoiceDecimalFormat;
import edu.sc.seis.sod.util.display.ParseRegions;
import edu.sc.seis.sod.util.display.ThreadSafeSimpleDateFormat;
import edu.sc.seis.sod.util.display.UnitDisplayUtil;
import edu.sc.seis.sod.util.time.ClockUtil;

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
        if(id == null) {
            return "null";
        }
        return ChannelIdUtil.toStringNoDates(id);
    }
    
    public static String dasherizeSiteCode(String s) {
        String out;
        if (s == null) {
            out = "--";
        } else {
            out = s.trim();
            if (out.length() == 0) {
                out = "--";
            }
        }
        return out;
    }

    public static String formatStation(StationId id) {
        return StationIdUtil.toStringNoDates(id);
    }

    public static String formatNetwork(StationId id) {
        //return NetworkIdUtil.toStringNoDates(id.getNetworkId());
        return id.getNetworkId();
    }

    public static String formatNetwork(Network net) {
        return net.toString();
    }

    public static String formatNetwork(NetworkId id) {
        return id.toString();
    }

    public static String formatNetworkYear(NetworkId id) {
        return ""+id.getStartYear();
    }

//    public static String networkName(NetworkAccess net) {
//        return networkName(net.get_attributes());
//    }

    public static String networkName(Network net) {
        return net.getDescription();
    }

    public static String stationName(Station station) {
        return station.getDescription(); // or getSite().getName()???
    }

    public static QuantityImpl getDepth(OriginImpl origin) {
        return getDepth(origin.getLocation());
    }

    public static QuantityImpl getDepth(Location loc) {
        return QuantityImpl.createQuantityImpl(loc.depth);
    }

    public static QuantityImpl getElevation(Location loc) {
        return QuantityImpl.createQuantityImpl(loc.elevation);
    }

    public static float getLatitude(Station station) {
        return station.getLatitude().getValue();
    }

    public static float getLatitude(OriginImpl origin) {
        return getLatitude(origin.getLocation());
    }

    public static float getLatitude(Location loc) {
        return loc.latitude;
    }

    public static String getLatitudeString(Location loc) {
        StringBuffer buf = new StringBuffer();
        buf.append(distFormat.format(Math.abs(loc.latitude)));
        buf.append(' ');
        buf.append((loc.latitude >= 0 ? 'N' : 'S'));
        return buf.toString();
    }

    public static float getLongitude(Station station) {
        return station.getLongitude().getValue();
    }

    public static float getLongitude(OriginImpl origin) {
        return getLongitude(origin.getLocation());
    }

    public static float getLongitude(Location loc) {
        return loc.longitude;
    }

    public static String getLongitudeString(Location loc) {
        StringBuffer buf = new StringBuffer();
        buf.append(distFormat.format(Math.abs(loc.longitude)));
        buf.append(' ');
        buf.append((loc.longitude >= 0 ? 'E' : 'W'));
        return buf.toString();
    }

    public static boolean isNull(Object obj) {
        if(obj == null)
            return true;
        return false;
    }

    public static boolean isEmpty(Object[] array) {
        return array.length == 0;
    }

    public static int length(Object[] array) {
        return array.length;
    }

    public static Instant getEffectiveBegin(Station station) {
        return station.getStartDateTime();
    }

    public static Instant getEffectiveEnd(Station station) {
        return station.getEndDateTime();
    }

    public static Instant getRangeBegin(TimeRange range) {
        return range.getBeginTime();
    }

    public static Instant getRangeEnd(TimeRange range) {
        return range.getEndTime();
    }

    public static QuantityImpl getDistance(ArrayList list) {
        return getDistance((Station)list.get(0), (OriginImpl)list.get(1));
    }

    public static QuantityImpl getDistance(Station station, OriginImpl origin) {
        if(station == null) {
            throw new NullPointerException("station is null");
        }
        if(origin == null) {
            throw new NullPointerException("origin is null");
        }
        if(origin.getLocation() == null) {
            throw new NullPointerException("origin.my_location is null");
        }
        return getDistance(Location.of(station), origin.getLocation());
    }

    public static QuantityImpl getDistance(Location from, Location to) {
        if(from == null) {
            throw new NullPointerException("from Location is null");
        }
        if(to == null) {
            throw new NullPointerException("to Location is null");
        }
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getDelta(), UnitImpl.DEGREE);
    }

    public static QuantityImpl getAzimuth(Station station, OriginImpl origin) {
        return getAzimuth(Location.of(station), origin.getLocation());
    }

    public static QuantityImpl getAzimuth(Location from, Location to) {
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getAz(), UnitImpl.DEGREE);
    }

    public static QuantityImpl getBackAzimuth(Station station, OriginImpl origin) {
        return getBackAzimuth(Location.of(station), origin.getLocation());
    }

    public static QuantityImpl getBackAzimuth(Location from, Location to) {
        DistAz d = new DistAz(from, to);
        return new QuantityImpl(d.getBaz(), UnitImpl.DEGREE);
    }

    public static String formatDate(Instant d) {
        synchronized(longFormat) {
            return longFormat.format(d);
        }
    }

    public static String formatDateForFile(Instant d) {
        synchronized(longFileFormat) {
            return longFileFormat.format(d);
        }
    }

    public static String formatDateForFile(OriginImpl origin) {
        return formatDateForFile(origin.getOriginTime());
    }

    public static String fancyFormat(Instant d) {
        synchronized(fancyFormat) {
            return fancyFormat.format(d);
        }
    }

    public static String formatYear(Instant d) {
        synchronized(yearDateFormat) {
            return yearDateFormat.format(d);
        }
    }

    public static String formatYMD(Instant d) {
        synchronized(ymdDateFormat) {
            return ymdDateFormat.format(d);
        }
    }

    public static Instant now() {
        return ClockUtil.now();
    }

    public static String filize(String base, String extension) {
        return filize(base + "." + extension);
    }

    public static String filize(String path) {
        if (path== null || path.length() == 0) {return "";}
        if(path.charAt(1) == ':' && path.charAt(2) == '\\') {
            return path.substring(0, 3) + filizeInternal(path.substring(3));
        }
        return filizeInternal(path);
    }

    private static String filizeInternal(String fileName) {
        fileName = fileName.replaceAll(" *\r?\n *", "");
        fileName = fileName.replaceAll("[ :,']", "_");
        fileName = fileName.replaceAll("[\t\f]", "");
        return fileName.trim();
    }

    private static TimeZone GMT = TimeZone.getTimeZone("GMT");
    
    public static ThreadSafeSimpleDateFormat ymdDateFormat = new ThreadSafeSimpleDateFormat("yyyy-MM-dd", GMT);

    public static ThreadSafeSimpleDateFormat yearDateFormat = new ThreadSafeSimpleDateFormat("yyyy", GMT);

    public static ThreadSafeSimpleDateFormat longFileFormat = new ThreadSafeSimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", GMT);

    public static ThreadSafeSimpleDateFormat longFormat = new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", GMT);

    public static ThreadSafeSimpleDateFormat mediumFormat = new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", GMT);

    private static ThreadSafeSimpleDateFormat fancyFormat = new ThreadSafeSimpleDateFormat("EEEE, d MMMM yyyy", GMT);

    public static ParseRegions pr = ParseRegions.getInstance();

    public static String formatDistance(QuantityImpl impl) {
        synchronized(distFormat) {
            return UnitDisplayUtil.formatQuantityImpl(impl, distFormat);
        }
    }

    public static String formatDepth(QuantityImpl impl) {
        synchronized(depthFormat) {
            return UnitDisplayUtil.formatQuantityImpl(impl, depthFormat, UnitImpl.KILOMETER);
        }
    }

    public static String formatElevation(QuantityImpl impl) {
        synchronized(depthFormat) {
            return UnitDisplayUtil.formatQuantityImpl(impl, depthFormat, UnitImpl.METER);
        }
    }

    public static NumberFormat getDepthFormat() {
        return depthFormat;
    }

    public static NumberFormat getDistFormat() {
        return distFormat;
    }

    private static NumberFormat distFormat = ChoiceDecimalFormat.createTomStyleA();

    private static NumberFormat depthFormat = ChoiceDecimalFormat.createTomStyleB();

    public static String oneLineAndClean(String in) {
        if (in == null) { return "";}
        return in.replaceAll("\\s+", " ").replaceAll("\"", "").trim();
    }
}