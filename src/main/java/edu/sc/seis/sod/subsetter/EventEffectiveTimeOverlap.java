package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.Time;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.SiteImpl;
import edu.sc.seis.sod.model.station.StationImpl;

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
        Time otime = event.getOrigin().getOriginTime();
        if (otime == null) {throw new RuntimeException("origin time is null");}
        MicroSecondDate originTime = new MicroSecondDate(otime);
        return new TimeRange(originTime,
                             originTime.add(DEFAULT_OFFSET));
    }

    public boolean overlaps(NetworkAttrImpl net) {
        return overlaps(net.getEffectiveTime());
    }

    public boolean overlaps(StationImpl station) {
        return overlaps(station.getEffectiveTime());
    }

    public boolean overlaps(SiteImpl site) {
        return overlaps(site.getEffectiveTime());
    }

    public boolean overlaps(ChannelImpl channel) {
        return overlaps(channel.getEffectiveTime());
    }

    static final TimeInterval DEFAULT_OFFSET = new TimeInterval(3, UnitImpl.DAY);
} // EventEffectiveTimeOverlap
