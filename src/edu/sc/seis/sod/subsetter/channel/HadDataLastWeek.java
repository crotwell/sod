package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.subsetter.dataCenter.FixedDataCenter;

/**
 * @author groves Created on May 6, 2005
 */
public class HadDataLastWeek implements ChannelSubsetter {

    public HadDataLastWeek(Element el) throws Exception {
        fixDC = new FixedDataCenter(DOMHelper.getElement(el, "fixedDataCenter"));
    }

    public boolean accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        // Make 7 requests for a day as the BUD likes it that way
        RequestFilter[] reqs = new RequestFilter[7];
        MicroSecondDate now = new MicroSecondDate();
        for(int i = 0; i < reqs.length; i++) {
            reqs[i] = new RequestFilter(channel.get_id(),
                                        now.subtract(makeDayInterval(i + 1))
                                                .getFissuresTime(),
                                        now.subtract(makeDayInterval(i))
                                                .getFissuresTime());
        }
        if(fixDC.getDataCenter().available_data(reqs).length > 0) {
            logger.debug(ChannelIdUtil.toStringNoDates(channel) + " had data");
            return true;
        }
        logger.debug(ChannelIdUtil.toStringNoDates(channel)
                + " didn't have data");
        return false;
    }

    private TimeInterval makeDayInterval(int days) {
        return new TimeInterval(days, UnitImpl.DAY);
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HadDataLastWeek.class);

    private FixedDataCenter fixDC;
}
