/*
 * Created on Jul 20, 2004
 */
package edu.sc.seis.sod.util.display;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.swing.text.DateFormatter;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.Magnitude;
import edu.sc.seis.sod.model.event.OriginImpl;

/**
 * @author oliverpa
 */
public class EventUtil {

    /**
     * This gets around the NoPreferredOrigin exception
     */
    @Deprecated
    public static OriginImpl extractOrigin(CacheEvent ev) {
        return ev.extractOrigin();
    }

    public static Magnitude[] sortByValue(Magnitude[] mags) {
        List compMagList = new ArrayList();
        for(int i = 0; i < mags.length; i++) {
            compMagList.add(new ComparableValueMagnitudeWrapper(mags[i]));
        }
        return sortAndExtract(compMagList);
    }

    public static Magnitude getSmallest(Magnitude[] mags) {
        return sortByValue(mags)[0];
    }

    public static Magnitude getLargest(Magnitude[] mags) {
        return sortByValue(mags)[mags.length - 1];
    }

    public static Magnitude[] sortByType(Magnitude[] mags) {
        List compMagList = new ArrayList();
        for(int i = 0; i < mags.length; i++) {
            compMagList.add(new ComparableTypeMagnitudeWrapper(mags[i]));
        }
        return sortAndExtract(compMagList);
    }

    public static Magnitude getBestByType(Magnitude[] mags) {
        return sortByType(mags)[0];
    }

    private static abstract class ComparableMagnitudeWrapper implements
            Comparable {

        public ComparableMagnitudeWrapper(Magnitude mag) {
            this.mag = mag;
        }

        Magnitude mag;
    }

    private static class ComparableTypeMagnitudeWrapper extends
            ComparableMagnitudeWrapper {

        public ComparableTypeMagnitudeWrapper(Magnitude mag) {
            super(mag);
        }

        public int compareTo(Object obj) {
            ComparableTypeMagnitudeWrapper compMag = (ComparableTypeMagnitudeWrapper)obj;
            if(PREFERRED_MAG_TYPES.contains(mag.type)) {
                if(PREFERRED_MAG_TYPES.contains(compMag.mag.type)) {
                    return PREFERRED_MAG_TYPES.indexOf(mag.type)
                            - PREFERRED_MAG_TYPES.indexOf(compMag.mag.type);
                }
                return -1;
            } else if(PREFERRED_MAG_TYPES.contains(compMag.mag.type)) {
                return 1;
            }
            return mag.type.compareTo(compMag.mag.type);
        }
    }

    private static class ComparableValueMagnitudeWrapper extends
            ComparableMagnitudeWrapper {

        public ComparableValueMagnitudeWrapper(Magnitude mag) {
            super(mag);
        }

        public int compareTo(Object obj) {
            ComparableValueMagnitudeWrapper compMag = (ComparableValueMagnitudeWrapper)obj;
            return Float.compare(mag.value, compMag.mag.value);
        }
    }

    private static Magnitude[] sortAndExtract(List wrappedComparableMagnitudes) {
        Collections.sort(wrappedComparableMagnitudes);
        Magnitude[] sortedMags = new Magnitude[wrappedComparableMagnitudes.size()];
        for(int i = 0; i < wrappedComparableMagnitudes.size(); i++) {
            sortedMags[i] = ((ComparableMagnitudeWrapper)wrappedComparableMagnitudes.get(i)).mag;
        }
        return sortedMags;
    }

    /**
     *  returns a string for the form "Event: Location | Time | Magnitude |
     *   Depth"
     */
    public static String getEventInfo(CacheEvent event) {
        return getEventInfo(event, NO_ARG_STRING);
    }

    public static String getEventInfo(CacheEvent event, String format) {
        return getEventInfo(event,
                            format,
                            TimeUtils.createFormatter("MM/dd/yyyy HH:mm:ss z"));
    }

    /**
     *  formats a string for the given event. To insert information about a
     *   certain item magic strings are used in the format string Magic Strings
     *   LOC adds the location of the event TIME adds the event time MAG adds
     *   event magnitude DEPTH adds the depth For example the string "Event: " +
     *   LOC + " | " + TIME + " | Mag: " + MAG + " | Depth: " + DEPTH + " " +
     *   DEPTH_UNIT produces the same thing as the no format call to
     *   getEventInfo
     */
    public static String getEventInfo(CacheEvent event,
                                      String format,
                                      DateTimeFormatter sdf) {
        OriginImpl origin = extractOrigin(event);
        StringBuffer buf = new StringBuffer(format);
        int index = buf.indexOf(LOC);
        if(index != -1) {
            // Get geographic name of origin
            ParseRegions regions = ParseRegions.getInstance();
            String location = regions.getGeographicRegionName(event.get_attributes().region.number);
            buf.delete(index, index + LOC.length());
            buf.insert(index, location);
        }
        return getOriginInfo(origin, buf.toString(), sdf);
    }

    /**
     *  returns a string for the form "Event: Location | Time | Magnitude |
     *   Depth"
     */
    public static String getOriginInfo(OriginImpl origin) {
        return getOriginInfo(origin, NO_ARG_STRING);
    }

    public static String getOriginInfo(OriginImpl origin, String format) {
        return getOriginInfo(origin,
                             format,
                             TimeUtils.createFormatter("MM/dd/yyyy HH:mm:ss z"));
    }

    public static String getOriginInfo(OriginImpl origin,
                                       String format,
                                       DateTimeFormatter sdf) {
        // Get Date and format it accordingly
        Instant msd = origin.getOriginTime();
        String originTimeString = msd.toString();
        // Get Magnitude
        float mag = Float.NaN;
        if(origin.getMagnitudes().length > 0) {
            mag = origin.getMagnitudes()[0].value;
        }
        // get depth
        QuantityImpl depth = origin.getLocation().depth;
        float latitude = origin.getLocation().latitude;
        float longitude = origin.getLocation().longitude;
        String catalog = origin.getCatalog();
        String contributor = origin.getContributor();
        StringBuffer buf = new StringBuffer(format);
        for(int i = 0; i < magicStrings.length; i++) {
            int index = buf.indexOf(magicStrings[i]);
            if(index != -1) {
                buf.delete(index, index + magicStrings[i].length());
                if(magicStrings[i].equals(TIME)) {
                    buf.insert(index, originTimeString);
                } else if(magicStrings[i].equals(MAG)) {
                    if(Float.isNaN(mag)) {
                        buf.insert(index, "...");
                    } else {
                        buf.insert(index, mag);
                    }
                } else if(magicStrings[i].equals(DEPTH)) {
                    buf.insert(index, depthFormatter.format(depth.getValue()));
                } else if(magicStrings[i].equals(DEPTH_UNIT)) {
                    buf.insert(index,
                               UnitDisplayUtil.getNameForUnit((UnitImpl)depth.getUnit()));
                } else if(magicStrings[i].equals(LAT)) {
                    buf.insert(index, latitude);
                } else if(magicStrings[i].equals(LON)) {
                    buf.insert(index, longitude);
                } else if(magicStrings[i].equals(CAT)) {
                    buf.insert(index, catalog);
                } else if(magicStrings[i].equals(CONTRIB)) {
                    buf.insert(index, contributor);
                }
            }
        }
        return buf.toString();
    }

    private static ThreadSafeDecimalFormat depthFormatter = new ThreadSafeDecimalFormat("###0.00");

    public static final String LOC = "LOC", TIME = "TIME", MAG = "MAG",
            DEPTH = "DEPTH", DEPTH_UNIT = "DEPTH_UNIT", LAT = "LAT",
            LON = "LON", CAT = "CAT", CONTRIB = "CONTRIB";

    private static final String[] magicStrings = {LOC,
                                                  TIME,
                                                  MAG,
                                                  DEPTH,
                                                  DEPTH_UNIT,
                                                  LAT,
                                                  LON,
                                                  CAT,
                                                  CONTRIB};

    public static final String NO_ARG_STRING = "Event: " + LOC + " | " + TIME
            + " | Mag: " + MAG + " | Depth " + DEPTH + " " + DEPTH_UNIT
            + " | (" + LAT + ", " + LON + ")";

    public static List PREFERRED_MAG_TYPES;
    static {
        String[] prefMagTypes = {"MO",
                                 "Mo",
                                 "MW",
                                 "Mw",
                                 "MS",
                                 "Ms",
                                 "MB",
                                 "Mb",
                                 "ML",
                                 "Ml",
                                 "M"};
        PREFERRED_MAG_TYPES = Arrays.asList(prefMagTypes);
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventUtil.class);
}
