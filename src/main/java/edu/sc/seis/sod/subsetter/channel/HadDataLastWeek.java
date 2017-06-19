package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.source.seismogram.ConstantSeismogramSourceLocator;
import edu.sc.seis.sod.source.seismogram.FdsnDataSelect;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.ClockUtil;

/**
 * @author groves Created on May 6, 2005
 */
public class HadDataLastWeek implements ChannelSubsetter {

    public HadDataLastWeek(Element el) throws Exception {
        Object sodObject = SodUtil.load(el, new String[] {"seismogram"});
        if(sodObject instanceof ConstantSeismogramSourceLocator) {
            dcLocator = (ConstantSeismogramSourceLocator)sodObject;
        } else {
            throw new ConfigurationException("Wrapped SeismogramSource must be instance of ConstantSeismogramSourceLocator");
        }
    }

    public StringTree accept(ChannelImpl channel, NetworkSource network)
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
        // Make 7 requests for a hour a day
        List<RequestFilter> reqs = new ArrayList<RequestFilter>();
        for(int i = 0; i < 7; i++) {
            reqs.add(new RequestFilter(channel.get_id(),
                                        now.subtract(makeDayInterval(i)).subtract(REQ_INTERVAL),
                                        now.subtract(makeDayInterval(i)) ));
        }
        if(dcLocator.getSeismogramSource().retrieveData(reqs).size() > 0) {
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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HadDataLastWeek.class);

    protected ConstantSeismogramSourceLocator dcLocator = new FdsnDataSelect();
    
    private static final TimeInterval REQ_INTERVAL = new TimeInterval(10, UnitImpl.MINUTE);

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

