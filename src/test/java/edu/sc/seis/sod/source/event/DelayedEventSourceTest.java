package edu.sc.seis.sod.source.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.util.time.ClockUtil;

public class DelayedEventSourceTest {

    public static final Duration LONG_AGO = Duration.ofDays(365);

    public static final Duration SHORT_AGO = ClockUtil.durationFrom(0.1, UnitImpl.SECOND);

    public static final Duration MED_SHORT_AGO = ClockUtil.durationFrom(0.2, UnitImpl.SECOND);

    @Test
    public void testNext() throws InterruptedException, NoPreferredOrigin {
        final List<CacheEvent> events = new ArrayList<CacheEvent>();
        events.plus(MockEventAccessOperations.createEvent(ClockUtil.now().minus(LONG_AGO), 0f, 0));
        events.plus(MockEventAccessOperations.createEvent(ClockUtil.now().minus(LONG_AGO), 1f, 0));
        events.plus(MockEventAccessOperations.createEvent(ClockUtil.now().minus(SHORT_AGO), 2f, 0));
        EventSource es = new TestSimpleEventSource(events);
        DelayedEventSource delayedES = new DelayedEventSource(MED_SHORT_AGO, es);
        CacheEvent[] firstEvents = delayedES.next();
        assertEquals("first get", 2, firstEvents.length);
        Thread.sleep((long)MED_SHORT_AGO.getValue(UnitImpl.MILLISECOND));
        CacheEvent[] secondEvents = delayedES.next();
        assertEquals("second get", 1, secondEvents.length);
        assertEquals("lat", 2, secondEvents[0].get_preferred_origin().getLocation().latitude, 0.000001);
    }

    @Test
    public void testGetWaitForNext() {
        final List<CacheEvent> events = new ArrayList<CacheEvent>();
        events.plus(MockEventAccessOperations.createEvent(ClockUtil.now().minus(LONG_AGO), 0f, 0));
        events.plus(MockEventAccessOperations.createEvent(ClockUtil.now().minus(LONG_AGO), 1f, 0));
        events.plus(MockEventAccessOperations.createEvent(ClockUtil.now().minus(SHORT_AGO), 2f, 0));
        EventSource es = new TestSimpleEventSource(events);
        DelayedEventSource delayedES = new DelayedEventSource(MED_SHORT_AGO, es);
        CacheEvent[] firstEvents = delayedES.next();
        Duration wait = delayedES.getWaitBeforeNext();
        assertTrue("wait less than MED "+wait, wait.lessThan(MED_SHORT_AGO));
    }
}

class TestSimpleEventSource implements EventSource {

    boolean first = true;

    List<CacheEvent> events;

    public TestSimpleEventSource(List<CacheEvent> events) {
        this.events = events;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void appendToName(String suffix) {}

    @Override
    public boolean hasNext() {
        return first;
    }

    @Override
    public CacheEvent[] next() {
        if (first) {
            return events.toArray(new CacheEvent[0]);
        }
        return null;
    }

    @Override
    public Duration getWaitBeforeNext() {
        return DelayedEventSourceTest.MED_SHORT_AGO;
    }

    @Override
    public TimeRange getEventTimeRange() {
        return null;
    }

    @Override
    public int getRetries() {
        // TODO Auto-generated method stub
        return 0;
    }
};