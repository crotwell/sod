package edu.sc.seis.sod.model.station;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.regex.Pattern;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;

/**
 * NetworkIdUtil.java Created: Wed Jan 31 13:07:45 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class NetworkIdUtil {

    public static boolean isTemporary(Network net) {
        return isTemporary(net.getNetworkCode());
    }

    public static boolean isTemporary(NetworkId id) {
        return isTemporary(id.networkCode);
    }

    public static boolean isTemporary(String code) {
        return tempNetPattern.matcher(code).matches();
    }

    private static Pattern tempNetPattern = Pattern.compile("[1-9XYZ].?");

    /**
     * Compares two networkIds. Dates are only checked for temporary networks.
     */
    @Deprecated
    public static boolean areEqual(String a, String b) {
        if(a.equals(b)) {
            return true;
        }
        return false;
    }
    
    /**
     * Compares two networkIds. Dates are only checked for temporary networks.
     */
    public static boolean areEqual(NetworkId a, NetworkId b) {
        if(!a.networkCode.equals(b.networkCode)) {
            return false;
        }
        // only compare dates if temp network, ie network code starts with X, Y,
        // Z or number
        return !isTemporary(a)
                || a.getStartYear() == b.getStartYear();
    }
    

    /**
     * Compares two networkIds. Dates are only checked for temporary networks.
     */
    public static boolean areEqual(Network a, Network b) {
        if(!a.getNetworkCode().equals(b.getNetworkCode())) {
            return false;
        }
        // only compare dates if temp network, ie network code starts with X, Y,
        // Z or number
        return !isTemporary(a.getNetworkCode())
                || a.getStartDateTime().equals(b.getStartDateTime());
    }
    
    public static final String DOT = ".";

    public static String formId(String netCode, Instant time) {
        String out = netCode;
        if (isTemporary(netCode)) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(time, TimeUtils.TZ_UTC);
            out += zdt.getYear();
        }
        return out;
    }

    
} // NetworkIdUtil
