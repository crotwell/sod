package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;

/**
 * EventEffectiveTimeOverlap.java Created: Wed Mar 19 10:49:54 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public class EventEffectiveTimeOverlap extends EffectiveTimeOverlap {

    public EventEffectiveTimeOverlap(EventAccessOperations event)
            throws NoPreferredOrigin {
        super(createTimeRange(event, offset));
    } // EventEffectiveTimeOverlap constructor

    static TimeRange createTimeRange(EventAccessOperations event,
                                     TimeInterval offset)
            throws NoPreferredOrigin {
        MicroSecondDate originTime = new MicroSecondDate(event.get_preferred_origin().origin_time);
        return new TimeRange(originTime.getFissuresTime(),
                             originTime.add(offset).getFissuresTime());
    }

    public static TimeInterval getOffset() {
        return offset;
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

    static final TimeInterval offset = new TimeInterval(3, UnitImpl.DAY);
} // EventEffectiveTimeOverlap
