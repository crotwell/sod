package edu.sc.seis.sod.subsetter.channel;

import java.util.HashMap;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.dataCenter.FixedDataCenter;

/**
 * @author groves Created on May 6, 2005
 */
public class HadDataLastWeek implements ChannelSubsetter {

    public HadDataLastWeek(Element el) throws Exception {
        fixDC = new FixedDataCenter(DOMHelper.getElement(el, "fixedDataCenter"));
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        MicroSecondDate now = ClockUtil.now();
        ChannelEffectiveTimeOverlap overlap = new ChannelEffectiveTimeOverlap(now.subtract(makeDayInterval(7)), now);
        if ( ! overlap.accept(channel, network).isSuccess()) {
            // fail expired channels quickly
            return new StringTreeLeaf(this, false, "Channel Ended");
        }
        String key = ChannelIdUtil.toStringNoDates(channel);
        if (recentRequests.containsKey(key)) {
            RecentRequest r = recentRequests.get(key);
            if (r.when.add(MAX_CACHE).after(ClockUtil.now())) {
                return new StringTreeLeaf(this, r.hadData);
            } else {
                recentRequests.remove(r);
            }
        }
        // Make 7 requests for a day as the BUD likes it that way
        RequestFilter[] reqs = new RequestFilter[7];
        for(int i = 0; i < reqs.length; i++) {
            reqs[i] = new RequestFilter(channel.get_id(),
                                        now.subtract(makeDayInterval(i + 1))
                                                .getFissuresTime(),
                                        now.subtract(makeDayInterval(i))
                                                .getFissuresTime());
        }
        if(fixDC.getDataCenter().available_data(reqs).length > 0) {
            logger.debug(ChannelIdUtil.toStringNoDates(channel) + " had data");
            recentRequests.put(key, new RecentRequest(channel.get_id(), ClockUtil.now(), true));
            return new StringTreeLeaf(this, true);
        }
        logger.debug(ChannelIdUtil.toStringNoDates(channel)
                + " didn't have data");
        recentRequests.put(key, new RecentRequest(channel.get_id(), ClockUtil.now(), false));
        return new StringTreeLeaf(this, false);
    }

    private TimeInterval makeDayInterval(int days) {
        return new TimeInterval(days, UnitImpl.DAY);
    }
    
    private HashMap<String, RecentRequest> recentRequests = new HashMap<String, RecentRequest>();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HadDataLastWeek.class);

    private FixedDataCenter fixDC;
    
    private TimeInterval MAX_CACHE = new TimeInterval(1, UnitImpl.HOUR);
}

class RecentRequest {
    
    ChannelId chanId;
    MicroSecondDate when;
    boolean hadData;
    
    RecentRequest(ChannelId chanId, MicroSecondDate when, boolean hadData) {
        this.chanId = chanId;
        this.when = when;
        this.hadData = hadData;
    }
}

