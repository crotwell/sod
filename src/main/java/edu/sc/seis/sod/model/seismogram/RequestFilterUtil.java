package edu.sc.seis.sod.model.seismogram;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sc.seis.sod.model.station.ChannelIdUtil;

/**
 * @author groves Created on Nov 2, 2004
 */
public class RequestFilterUtil {

    public static boolean areEqual(RequestFilter one, RequestFilter two) {
        if(one == two) { return true; }
        return ChannelIdUtil.areEqual(one.channelId, two.channelId)
                && one.startTime.equals(two.startTime)
                && one.endTime.equals(two.endTime);
    }

    public static boolean containsWildcard(RequestFilter rf) {
        return rf.channelId.getNetworkId().equals("*") ||
        rf.channelId.getStationCode().equals("*") ||
        rf.channelId.getLocCode().equals("*") ||
        rf.channelId.getChannelCode().equals("*");
    }

    public static boolean containsWildcard(List<RequestFilter> rfList) {
        for (RequestFilter rf : rfList) {
            if (containsWildcard(rf)) {return true;}
        }
        return false;
    }

    public static Map<String, List<RequestFilter>> splitByChannel(List<RequestFilter> rf) {
        HashMap<String, List<RequestFilter>> out = new HashMap<String, List<RequestFilter>>();
        for (RequestFilter requestFilter : rf) {
            String key = ChannelIdUtil.toStringNoDates(requestFilter.channelId);
            if (!out.containsKey(key)) {
                out.put(key, new ArrayList<RequestFilter>());
            }
            out.get(key).add(requestFilter);
        }
        return out;
    }

    public static String toString(RequestFilter[] rf) {
        if (rf == null) { return "empty request/n";}
        return toString(Arrays.asList(rf));
    }

    public static String toString(RequestFilter[][] rf) {
        if (rf == null) { return "empty request/n";}
        String out = "";
        for (int i = 0; rf != null && i < rf.length; i++) {
            out += i + "\n" + toString(Arrays.asList(rf[i])) + "\n";
        }
        return out;
    }

    public static String toString(List<RequestFilter> rf) {
        if (rf == null) { return "empty request/n";}
        String s = "Request length=" + rf.size() + "\n";
        for (RequestFilter requestFilter : rf) {
            s += toString(requestFilter) + "\n";
        }
        return s;
    }

    public static String toString(RequestFilter rf) {
        return ChannelIdUtil.toStringNoDates(rf.channelId) + " from "
                + rf.startTime + " to " + rf.endTime;
    }

    public static RequestFilter[] removeSmallRequests(RequestFilter[] rf, Duration minSize) {
        List<RequestFilter> out = new ArrayList<RequestFilter>();
        for (int i = 0; i < rf.length; i++) {
            if (rf[i].getDuration().compareTo(minSize) > 0) {
                out.add(rf[i]);
            }
        }
        return out.toArray(new RequestFilter[0]);
    }
}