package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;


public class CoarseFdsnAvailableData {
    
    public void append(String host, ChannelId chan, MicroSecondTimeRange range) {
        getList(host, chan).add(range);
    }
    
    public void update(String host, ChannelId chan, List<MicroSecondTimeRange> rangeList) {
        Map<String, List<MicroSecondTimeRange>> hostCache = getHostCache(host);
        hostCache.put(ChannelIdUtil.toString(chan), rangeList);
        System.out.println("Update Avail: "+rangeList.size()+"  "+(rangeList.size()>0?rangeList.get(0):""));
    }
    
    public List<MicroSecondTimeRange> get(String host, ChannelId chan) {
        return getList(host, chan);
    }
    
    public boolean overlaps(String host, ChannelId chan, MicroSecondTimeRange range) {
        List<MicroSecondTimeRange> chanList = get(host, chan);
        for (MicroSecondTimeRange dataRange : chanList) {
            if (range.intersects(dataRange)) {
                return true;
            }
        }
        return false;
    }
    
    private Map<String, List<MicroSecondTimeRange>> getHostCache(String host) {
        // assume host uniquely identifies the fdsnStation to fdsnDataSelect mapping
        Map<String, List<MicroSecondTimeRange>> hostCache = cache.get(host);
        if (hostCache == null) {
            hostCache = new HashMap<String, List<MicroSecondTimeRange>>();
            cache.put(host, hostCache);
        }
        return hostCache;
    }
    
    private List<MicroSecondTimeRange> getList(String host, ChannelId chan) {
        Map<String, List<MicroSecondTimeRange>> hostCache = getHostCache(host);
        String chanIdStr = ChannelIdUtil.toString(chan);
        List<MicroSecondTimeRange> chanCache = hostCache.get(chanIdStr);
        if (chanCache == null) {
            chanCache = new ArrayList<MicroSecondTimeRange>();
            hostCache.put(chanIdStr, chanCache);
        }
        return chanCache;
    }
    
    Map<String, Map<String, List<MicroSecondTimeRange>>> cache = new HashMap<String, Map<String, List<MicroSecondTimeRange>>>();
}
