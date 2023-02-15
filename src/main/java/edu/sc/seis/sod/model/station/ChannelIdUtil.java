package edu.sc.seis.sod.model.station;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.TimeFormatter;


/**
 * ChannelIdUtil.java Created: Wed Jan 24 14:33:39 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class ChannelIdUtil {

    public static boolean areEqual(Channel a, ChannelId b) {
        return areEqual(b, a);
    }
    public static boolean areEqual(ChannelId a, Channel b) {
        return areEqual(a, ChannelId.of(b));
    }
    
    public static boolean areEqual(ChannelId a, ChannelId b) {
        return a.getStationCode().equals(b.getStationCode())
                && a.getLocCode().equals(b.getLocCode())
                && a.getChannelCode().equals(b.getChannelCode())
                && a.getNetworkId().equals(b.getNetworkId())
                && a.getStartTime().equals(b.getStartTime());
    }
    
    public static boolean areEqualExceptForBeginTime(ChannelId a, Channel b) {
        return areEqualExceptForBeginTime(a, ChannelId.of(b));
    }
    
    public static boolean areEqualExceptForBeginTime(ChannelId a, ChannelId b) {
        return a.getStationCode().equals(b.getStationCode())
        && a.getLocCode().equals(b.getLocCode())
        && a.getChannelCode().equals(b.getChannelCode())
        && a.getNetworkId().equals(b.getNetworkId());
    }
    
    public static boolean areEqual(Channel a, Channel b) {
        return areEqual(new ChannelId(a), new ChannelId(b)) &&
        a.getAzimuth().getValue() == b.getAzimuth().getValue() &&
        a.getDip().getValue() == b.getDip().getValue() &&
        a.getSampleRate().getValue() == b.getSampleRate().getValue();
    }

    public static String toStringNoDates(ChannelId id) {
        return id.getNetworkId() + NetworkIdUtil.DOT
                + id.getStationCode() + NetworkIdUtil.DOT + id.getLocCode() + NetworkIdUtil.DOT + id.getChannelCode();
    }

    public static String toStringNoDates(Channel chan) {
        return toStringNoDates(new ChannelId(chan));
    }

    public static String toString(Channel chan) {
        return chan.getNetwork().getNetworkId()
                + NetworkIdUtil.DOT
                + chan.getStationCode()
                + NetworkIdUtil.DOT
                + chan.getLocCode()
                + NetworkIdUtil.DOT
                + chan.getChannelCode()
                + NetworkIdUtil.DOT
                + chan.getStartDate();
    }

    public static String toString(ChannelId id) {
        return id.getNetworkId()
                + NetworkIdUtil.DOT
                + id.getStationCode()
                + NetworkIdUtil.DOT
                + id.getLocCode()
                + NetworkIdUtil.DOT
                + id.getChannelCode()
                + NetworkIdUtil.DOT
                + id.getStartTime();
    }

    public static ChannelId fromString(String s) {
        Pattern fdsnPattern = Pattern.compile("FDSN:([0-9A-Z]{1-8})\\.([0-9A-Z]{1,8})\\.([0-9A-Z]{1-8}:)+([0-9A-Z]{3,4})_(\\d{8}T\\d{6}\\.\\d{1-9}Z)");
        Matcher matcher = fdsnPattern.matcher(s);
        if (matcher.matches()) {
        return new ChannelId(matcher.group(1),
                             matcher.group(2),
                             matcher.group(3),
                             matcher.group(4),
                             TimeUtils.parseISOString( matcher.group(5)));
        }
        throw new RuntimeException("Doesn't match a channel id pattern: ");
    }

    public static String toStringFormatDates(ChannelId id) {
        return id.getNetworkId() + NetworkIdUtil.DOT
                + id.getStationCode() + NetworkIdUtil.DOT + id.getLocCode() + NetworkIdUtil.DOT + id.getChannelCode()
                + NetworkIdUtil.DOT + TimeFormatter.format(id.getStartTime());
    }
    
    public static String toStringFormatDates(Channel chan) {
        return chan.getNetworkId() + NetworkIdUtil.DOT
                + chan.getStationCode() + NetworkIdUtil.DOT + chan.getLocCode() + NetworkIdUtil.DOT + chan.getChannelCode()
                + NetworkIdUtil.DOT + TimeFormatter.format(chan.getStartDateTime());
    }
    
    public static String getBandCode(ChannelId id) {
        return getBandCode(id.getChannelCode());
    }
    
    public static String getBandCode(String channelCode) {
        return ""+channelCode.charAt(0);
    }
    
    public static String getGainCode(ChannelId id) {
        return getGainCode(id.getChannelCode());
    }
    
    public static String getGainCode(String channelCode) {
        return ""+channelCode.charAt(1);
    }
    
    public static String getOrientationCode(ChannelId id) {
        return getOrientationCode(id.getChannelCode());
    }
    
    public static String getOrientationCode(String channelCode) {
        return ""+channelCode.charAt(2);
    }

    /**
     * Calculates a default azimuth based on the orientation code,
     * 0 for Z and N, 90 for E, -1 otherwise
     * @param chanCode
     * @return
     */
    public static int getDefaultAzimuth(String chanCode) {
        if (chanCode.endsWith("Z") || chanCode.endsWith("N")) {
            return 0;
        } else if (chanCode.endsWith("E")) {
            return 90;
        }
        return -1;
    }
    
    /**
     * Calculates a default dip based on the orientation code,
     * -90 for Z, 0 for N and E, -1 otherwise
     * @param chanCode
     * @return
     */
    public static int getDefaultDip(String chanCode) {
        if (chanCode.endsWith("E") || chanCode.endsWith("N")) {
            return 0;
        } else if (chanCode.endsWith("Z")) {
            return -90;
        }
        return -1;
    }
    
    public static float minSPSForBandCode(String bandCode) {
        return minSPSForBandCode(bandCode.charAt(0));
    }
    
    public static float minSPSForBandCode(char bandCode) {
        float minSps = 0;
        switch (bandCode) {
            case 'F': 
            case 'G': 
                minSps = 1000;
                break;
            case 'D': 
            case 'C': 
                minSps = 250;
                break;
            case 'E': 
                minSps = 80;
                break;
            case 'S': 
                minSps = 10;
                break;
            case 'H': 
                minSps = 80;
                break;
            case 'B': 
                minSps = 10;
                break;
            case 'M': 
                minSps = 1;
                break;
            case 'L': 
                minSps = 1;
                break;
            case 'V': 
                minSps = 0.1f;
                break;
            case 'U': 
                minSps = 0.01f;
                break;
            case 'R': 
                minSps = 0.001f;
                break;
            default: 
                minSps = 1;
        }
        return minSps;
    }

    public static int hashCode(ChannelId id) {
        return 12 + toString(id).hashCode();
    }
} // ChannelIdUtil
