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

    protected PhaseRequest(String beginPhase, String endPhase, String model)
            throws ConfigurationException {
        this.beginPhase = beginPhase;
        this.endPhase = endPhase;
        try {
            util = TauPUtil.getTauPUtil(model);
        } catch(TauModelException e) {
            throw new ConfigurationException(model
                    + " caused a TauModelException", e);
        }
    }

    public PhaseRequest(String beginPhase,
                        TimeInterval beginOffest,
                        String endPhase,
                        TimeInterval endOffset,
                        String model) throws ConfigurationException {
        this(beginPhase, endPhase, model);
        this.beginOffset = beginOffest;
        this.endOffset = endOffset;
    }

    public PhaseRequest(Element config) throws ConfigurationException {
        this(DOMHelper.extractText(config, "beginPhase"),
             DOMHelper.extractText(config, "endPhase"),
             DOMHelper.extractText(config, "model", "prem"));
        Element beginEl = DOMHelper.extractElement(config, "beginOffset");
        if(DOMHelper.hasElement(beginEl, "ratio")) {
            beginOffsetRatio = DOMHelper.extractDouble(beginEl, "ratio", 1.0);
            beginOffsetRatioMinimum = SodUtil.loadTimeInterval(DOMHelper.getElement(beginEl,
                                                                                    "minimum"));
            if(DOMHelper.hasElement(beginEl, "negative")) {
                negateBeginOffsetRatio = true;
            }
        } else {
            beginOffset = SodUtil.loadTimeInterval(beginEl);
        }
        Element endEl = DOMHelper.extractElement(config, "endOffset");
        if(DOMHelper.hasElement(endEl, "ratio")) {
            endOffsetRatio = DOMHelper.extractDouble(endEl, "ratio", 1.0);
            endOffsetRatioMinimum = SodUtil.loadTimeInterval(DOMHelper.getElement(endEl,
                                                                                  "minimum"));
            if(DOMHelper.hasElement(endEl, "negative")) {
                negateEndOffsetRatio = true;
            }
        } else {
            endOffset = SodUtil.loadTimeInterval(endEl);
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
        TimeInterval bInterval = new TimeInterval(begin, UnitImpl.SECOND);
        TimeInterval eInterval = new TimeInterval(end, UnitImpl.SECOND);
        MicroSecondDate bDate = originDate.add(bInterval);
        MicroSecondDate eDate = originDate.add(eInterval);
        if(beginOffset != null) {
            bInterval = beginOffset;
        } else {
            bInterval = getTimeIntervalFromRatio(bDate,
                                                 eDate,
                                                 beginOffsetRatio,
                                                 beginOffsetRatioMinimum,
                                                 negateBeginOffsetRatio);
        }
        if(endOffset != null) {
            eInterval = endOffset;
        } else {
            eInterval = getTimeIntervalFromRatio(bDate,
                                                 eDate,
                                                 endOffsetRatio,
                                                 endOffsetRatioMinimum,
                                                 negateEndOffsetRatio);
        }
        bDate = bDate.add(bInterval);
        eDate = eDate.add(eInterval);
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

    public static TimeInterval getTimeIntervalFromRatio(MicroSecondDate startPhaseTime,
                                                        MicroSecondDate endPhaseTime,
                                                        double ratio,
                                                        TimeInterval minimumTime,
                                                        boolean negate) {
        TimeInterval interval = new TimeInterval(endPhaseTime.difference(startPhaseTime)
                .multiplyBy(ratio));
        if(interval.lessThan(minimumTime)) {
            return negateIfTrue(minimumTime, negate);
        }
        return negateIfTrue(interval, negate);
    }

    public static TimeInterval negateIfTrue(TimeInterval interval,
                                            boolean negate) {
        if(negate) {
            double value = interval.getValue();
            return new TimeInterval(-value, interval.getUnit());
        }
        return interval;
    }

    private String beginPhase, endPhase;

    private TimeInterval beginOffset, endOffset;

    private double beginOffsetRatio, endOffsetRatio;

    private TimeInterval beginOffsetRatioMinimum, endOffsetRatioMinimum;

    private boolean negateBeginOffsetRatio = false,
            negateEndOffsetRatio = false;

    private TauPUtil util;

    private RequestFilter prevRequestFilter;

    private Location prevOriginLoc, prevSiteLoc;

    private static Logger logger = Logger.getLogger(PhaseRequest.class);

    private static final String ORIGIN = "origin";
}// PhaseRequest
