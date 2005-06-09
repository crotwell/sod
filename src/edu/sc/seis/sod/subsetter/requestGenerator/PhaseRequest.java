package edu.sc.seis.sod.subsetter.requestGenerator;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class PhaseRequest implements RequestGenerator {

    public PhaseRequest(Element config) throws ConfigurationException {
        beginPhase = DOMHelper.extractText(config, "beginPhase");
        endPhase = DOMHelper.extractText(config, "endPhase");
        Element beginEl = DOMHelper.extractElement(config, "beginOffset");
        beginOffset = SodUtil.loadTimeInterval(beginEl);
        Element endEl = DOMHelper.extractElement(config, "endOffset");
        endOffset = SodUtil.loadTimeInterval(endEl);
        String model = DOMHelper.extractText(config, "model", "prem");
        try {
            util = TauPUtil.getTauPUtil(model);
        } catch(TauModelException e) {
            throw new ConfigurationException(model
                    + " caused a TauModelException", e);
        }
    }

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel,
                                           CookieJar jar) throws Exception {
        RequestFilter rf = generateRequest(event, channel);
        if(rf == null) {
            return new RequestFilter[0];
        }
        return new RequestFilter[] {generateRequest(event, channel)};
    }

    public RequestFilter generateRequest(EventAccessOperations event,
                                         Channel channel) throws Exception {
        Origin origin = EventUtil.extractOrigin(event);
        if(prevRequestFilter != null
                && origin.my_location.equals(prevOriginLoc)
                && channel.my_site.my_location.equals(prevSiteLoc)) {
            // don't need to do any work
            return new RequestFilter(channel.get_id(),
                                     prevRequestFilter.start_time,
                                     prevRequestFilter.end_time);
        }
        prevOriginLoc = origin.my_location;
        prevSiteLoc = channel.my_site.my_location;
        prevRequestFilter = null;
        double begin = getArrivalTime(beginPhase, channel, origin);
        double end = getArrivalTime(endPhase, channel, origin);
        if(begin == -1 || end == -1) {
            // no arrivals found, return zero length request filters
            return null;
        }
        MicroSecondDate originDate = new MicroSecondDate(origin.origin_time);
        TimeInterval bInterval = beginOffset.add(new TimeInterval(begin,
                                                                  UnitImpl.SECOND));
        TimeInterval eInterval = endOffset.add(new TimeInterval(end,
                                                                UnitImpl.SECOND));
        MicroSecondDate bDate = originDate.add(bInterval);
        MicroSecondDate eDate = originDate.add(eInterval);
        prevRequestFilter = new RequestFilter(channel.get_id(),
                                              bDate.getFissuresTime(),
                                              eDate.getFissuresTime());
        logger.debug("Generated request from "
                + bDate
                + " to "
                + eDate
                + " for "
                + StationIdUtil.toStringNoDates(channel.my_site.my_station.get_id()));
        return prevRequestFilter;
    }

    private double getArrivalTime(String phase, Channel chan, Origin origin)
            throws TauModelException {
        if(phase.equals(ORIGIN)) {
            return 0;
        }
        String[] phases = {phase};
        Arrival[] arrivals = util.calcTravelTimes(chan.my_site.my_location,
                                                  origin,
                                                  phases);
        if(arrivals.length == 0) {
            return -1;
        }
        // round to milliseconds
        return Math.rint(1000 * arrivals[0].getTime()) / 1000;
    }

    private String beginPhase, endPhase;

    private TimeInterval beginOffset, endOffset;

    private TauPUtil util;

    private RequestFilter prevRequestFilter;

    private Location prevOriginLoc, prevSiteLoc;

    private static Logger logger = Logger.getLogger(PhaseRequest.class);

    private static final String ORIGIN = "origin";
}// PhaseRequest
