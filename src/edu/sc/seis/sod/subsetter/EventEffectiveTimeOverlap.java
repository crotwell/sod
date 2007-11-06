package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.database.event.StatefulEvent;

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
        Time otime = event.getOrigin().origin_time;
        if (otime == null) {throw new RuntimeException("origin time is null");}
        MicroSecondDate originTime = new MicroSecondDate(otime);
        return new TimeRange(originTime.getFissuresTime(),
                             originTime.add(DEFAULT_OFFSET).getFissuresTime());
    }

    public boolean overlaps(NetworkAttr net) {
        return overlaps(net.effective_time);
    }

    public boolean overlaps(Station station) {
        return overlaps(station.effective_time);
    }

    public boolean overlaps(Site site) {
        return overlaps(site.effective_time);
    }

    public boolean overlaps(Channel channel) {
        return overlaps(channel.effective_time);
    }

    static final TimeInterval DEFAULT_OFFSET = new TimeInterval(3, UnitImpl.DAY);
} // EventEffectiveTimeOverlap
