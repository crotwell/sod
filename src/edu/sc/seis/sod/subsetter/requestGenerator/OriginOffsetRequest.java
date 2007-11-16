package edu.sc.seis.sod.subsetter.requestGenerator;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

/**
 * OriginOffsetRequest.java Created: Wed Apr 2 16:06:39 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public class OriginOffsetRequest implements RequestGenerator {

    public OriginOffsetRequest(Element config) throws ConfigurationException {
        Element beginEl = DOMHelper.extractElement(config, "beginOffset");
        beginOffset = SodUtil.loadTimeInterval(beginEl);
        Element endEl = DOMHelper.extractElement(config, "endOffset");
        endOffset = SodUtil.loadTimeInterval(endEl);
    }

    public RequestFilter[] generateRequest(CacheEvent event,
                                           Channel channel,
                                           CookieJar cookieJar)
            throws Exception {
        Origin origin = EventUtil.extractOrigin(event);
        MicroSecondDate originDate = new MicroSecondDate(origin.origin_time);
        MicroSecondDate bDate = originDate.add(beginOffset);
        MicroSecondDate eDate = originDate.add(endOffset);
        RequestFilter[] filters = {new RequestFilter(channel.get_id(),
                                                     bDate.getFissuresTime(),
                                                     eDate.getFissuresTime())};
        return filters;
    }

    private TimeInterval beginOffset, endOffset;
} // OriginOffsetRequest
