package edu.sc.seis.sod.source.event;

import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.SodElement;

public interface EventSource extends SodElement {

    /**
     * @return - false when this event source will return no new events from
     *         calls to next
     */
    public boolean hasNext();

    /**
     * @return - the next set of events. Events can be returned from this method
     *         multiple times
     */
    public CacheEvent[] next();

    /**
     * If the source needs to wait for more events to arrive, it should return
     * the amount of time it wants to wait with this method.
     */
    public TimeInterval getWaitBeforeNext();

    /**
     * @return - a MicroSecondTimeRange starting at least as early as the
     *         earliest event and ending at least as late as the latest event.
     *         No event should be returned by this source with an origin before
     *         the begin time or after the end time of this range.
     */
    public MicroSecondTimeRange getEventTimeRange();
    
    public String getDescription();
}
