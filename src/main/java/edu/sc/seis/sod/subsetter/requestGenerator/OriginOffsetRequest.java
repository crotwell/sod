package edu.sc.seis.sod.subsetter.requestGenerator;

import java.time.Duration;
import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.util.display.EventUtil;

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
        OriginImpl origin = EventUtil.extractOrigin(event);
        Instant originDate = origin.getOriginTime();
        Instant bDate = originDate.plus(beginOffset);
        Instant eDate = originDate.plus(endOffset);
        RequestFilter[] filters = {new RequestFilter(channel,
                                                     bDate,
                                                     eDate)};
        return filters;
    }

    private Duration beginOffset, endOffset;
} // OriginOffsetRequest
