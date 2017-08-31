package edu.sc.seis.sod.subsetter;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.event.StatefulEvent;

/**
 * EventEffectiveTimeOverlap.java Created: Wed Mar 19 10:49:54 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public class EventEffectiveTimeOverlap extends EffectiveTimeOverlap {

    public EventEffectiveTimeOverlap(StatefulEvent event)
            throws NoPreferredOrigin {
        super(createTimeRange(event));
    } // EventEffectiveTimeOverlap constructor

    static TimeRange createTimeRange(StatefulEvent event)
            throws NoPreferredOrigin {
        Instant originTime = event.getOrigin().getOriginTime();
        if (originTime == null) {throw new RuntimeException("origin time is null");}
        return new TimeRange(originTime,
                             originTime.plus(DEFAULT_OFFSET));
    }

    public boolean overlaps(Network net) {
        return overlaps(net.getEffectiveTime());
    }

    public boolean overlaps(Station station) {
        return overlaps(station.getEffectiveTime());
    }

    public boolean overlaps(Channel channel) {
        return overlaps(channel.getEffectiveTime());
    }

    static final Duration DEFAULT_OFFSET = Duration.ofDays(3);
} // EventEffectiveTimeOverlap
