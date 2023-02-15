package edu.sc.seis.sod.bag;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.LocationUtil;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.util.display.EventUtil;

public class PhaseRequest  {

    protected PhaseRequest(String beginPhase, String endPhase, String model)
            throws TauModelException {
        this.beginPhase = beginPhase;
        this.endPhase = endPhase;
            util = TauPUtil.getTauPUtil(model);
    }

    public PhaseRequest(String beginPhase,
                        Duration beginOffest,
                        String endPhase,
                        Duration endOffset,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = beginOffest;
        this.endOffset = endOffset;
    }
    
    public PhaseRequest(String beginPhase,
                        Duration beginOffset,
                        String endPhase,
                        double endOffestRatio,
                        Duration endOffsetMinimum,
                        boolean negateEndOffsetRatio,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = beginOffset;
        this.endOffset = null;
        this.endOffsetRatio = endOffestRatio;
        this.endOffsetRatioMinimum = endOffsetMinimum;
        this.negateEndOffsetRatio = negateEndOffsetRatio;
    }
    
    public PhaseRequest(String beginPhase,
                        double beginOffestRatio,
                        Duration beginOffsetMinimum,
                        boolean negateBeginOffsetRatio,
                        String endPhase,
                        Duration endOffset,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = null;
        this.beginOffsetRatio = beginOffestRatio;
        this.beginOffsetRatioMinimum = beginOffsetMinimum;
        this.negateBeginOffsetRatio = negateBeginOffsetRatio;
        this.endOffset = endOffset;
    }
    
    public PhaseRequest(String beginPhase,
                        double beginOffestRatio,
                        Duration beginOffsetMinimum,
                        boolean negateBeginOffsetRatio,
                        String endPhase,
                        double endOffestRatio,
                        Duration endOffsetMinimum,
                        boolean negateEndOffsetRatio,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = null;
        this.beginOffsetRatio = beginOffestRatio;
        this.beginOffsetRatioMinimum = beginOffsetMinimum;
        this.negateBeginOffsetRatio = negateBeginOffsetRatio;
        this.endOffset = null;
        this.endOffsetRatio = endOffestRatio;
        this.endOffsetRatioMinimum = endOffsetMinimum;
        this.negateEndOffsetRatio = negateEndOffsetRatio;
    }

    public RequestFilter generateRequest(CacheEvent event,
                                         Channel channel) throws Exception {
        OriginImpl origin = EventUtil.extractOrigin(event);

        if (channel.getStationCode() == null) {
            throw new Error("station is null "+channel);
        }
        synchronized(this) {
            if(prevRequestFilter != null
                    && origin.getOriginTime().equals( prevOriginTime)
                    && LocationUtil.areEqual(origin.getLocation(), prevOriginLoc)
                    && LocationUtil.areSameLocation(channel, prevSiteLoc)) {
                // don't need to do any work
                return new RequestFilter(channel,
                                         prevRequestFilter.startTime,
                                         prevRequestFilter.endTime);
            }
        }
        double begin = getArrivalTime(beginPhase, channel, origin);
        double end = getArrivalTime(endPhase, channel, origin);
        if(begin == -1 || end == -1) {
            // no arrivals found, return zero length request filters
            return null;
        }
        Instant originDate = origin.getOriginTime();
        Instant bDate = originDate.plus(TimeUtils.durationFromSeconds(begin));
        Instant eDate = originDate.plus(TimeUtils.durationFromSeconds(end));

        Duration bInterval;
        Duration eInterval;
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
        bDate = bDate.plus(bInterval);
        eDate = eDate.plus(eInterval);
        synchronized(this) {
            prevOriginLoc = origin.getLocation();
            prevSiteLoc = Location.of(channel);
            prevOriginTime = origin.getOriginTime();
            if (channel.getStationCode() == null) {
                throw new Error("station is null "+channel);
            }
            prevRequestFilter = new RequestFilter(channel,
                                                  bDate,
                                                  eDate);
            if (prevRequestFilter.getChannelId().getStationCode() == null) {
                throw new Error("station is null "+prevRequestFilter);
            }
        }
        logger.info("Generated request from "
                + bDate
                + " to "
                + eDate
                + " for "
                + StationIdUtil.toStringNoDates(channel.getStation()));
        if (prevRequestFilter.getChannelId().getStationCode() == null) {
            throw new Error("station is null "+prevRequestFilter);
        }
        return prevRequestFilter;
    }

    private double getArrivalTime(String phase, Channel chan, OriginImpl origin)
            throws TauModelException {
        if(phase.equals(ORIGIN)) {
            return 0;
        }
        String[] phases = {phase};
        List<Arrival> arrivals = util.calcTravelTimes(chan,
                                                  origin,
                                                  phases);
        if(arrivals.size() == 0) {
            return -1;
        }
        // round to milliseconds
        return Math.rint(1000 * arrivals.get(0).getTime()) / 1000;
    }

    public static Duration getTimeIntervalFromRatio(Instant startPhaseTime,
                                                    Instant endPhaseTime,
                                                        double ratio,
                                                        Duration minimumTime,
                                                        boolean negate) {
        Duration interval = Duration.ofNanos(Math.round(Duration.between(startPhaseTime,  endPhaseTime).toNanos()*ratio));
        if(interval.toNanos() < minimumTime.toNanos()) {
            return negateIfTrue(minimumTime, negate);
        }
        return negateIfTrue(interval, negate);
    }

    public static Duration negateIfTrue(Duration interval,
                                            boolean negate) {
        if(negate) {
            return interval.negated();
        }
        return interval;
    }
    
    public String getBeginPhase() {
        return beginPhase;
    }
    
    public String getEndPhase() {
        return endPhase;
    }
    
    public Duration getBeginOffset() {
        return beginOffset;
    }
    
    public Duration getEndOffset() {
        return endOffset;
    }
    
    public double getBeginOffsetRatio() {
        return beginOffsetRatio;
    }
    
    public double getEndOffsetRatio() {
        return endOffsetRatio;
    }
    
    public Duration getBeginOffsetRatioMinimum() {
        return beginOffsetRatioMinimum;
    }
    
    public Duration getEndOffsetRatioMinimum() {
        return endOffsetRatioMinimum;
    }
    
    public boolean isNegateBeginOffsetRatio() {
        return negateBeginOffsetRatio;
    }
    
    public boolean isNegateEndOffsetRatio() {
        return negateEndOffsetRatio;
    }
    
    private String beginPhase, endPhase;

    private Duration beginOffset, endOffset;

    private double beginOffsetRatio, endOffsetRatio;

    private Duration beginOffsetRatioMinimum, endOffsetRatioMinimum;

    private boolean negateBeginOffsetRatio = false,
            negateEndOffsetRatio = false;

    private TauPUtil util;

    private RequestFilter prevRequestFilter;

    private Location prevOriginLoc, prevSiteLoc;
    
    private Instant prevOriginTime;

    private static Logger logger = LoggerFactory.getLogger(PhaseRequest.class);

    private static final String ORIGIN = "origin";
}// PhaseRequest
