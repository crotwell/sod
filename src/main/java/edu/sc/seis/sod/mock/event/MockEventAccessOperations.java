package edu.sc.seis.sod.mock.event;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.mock.MockLocation;
import edu.sc.seis.sod.mock.MockParameterRef;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.Magnitude;
import edu.sc.seis.sod.model.event.OriginImpl;

public class MockEventAccessOperations {

    public synchronized static CacheEvent[] createEvents() {
        return new CacheEvent[] {createEvent(), createFallEvent()};
    }

    public static CacheEvent createEvent() {
        return createEvent(MockOrigin.create(), MockEventAttr.create());
    }

    public static CacheEvent createEvent(Instant time, float lat, float lon) {
        return createEvent(MockOrigin.create(time, lat, lon), MockEventAttr.create());
    }

    public static CacheEvent createFallEvent() {
        return createEvent(MockOrigin.createWallFallOrigin(),
                           MockEventAttr.createWallFallAttr());
    }

    public static CacheEvent createEvent(Instant eventTime,
                                         int magnitudeAndDepth,
                                         int feRegion) {
        Magnitude[] mags = {new Magnitude("test", magnitudeAndDepth, "another")};
        return createEvent(MockOrigin.create(eventTime, mags),
                           MockEventAttr.create(feRegion));
    }

    public static CacheEvent createEvent(OriginImpl origin, EventAttrImpl attr) {
        OriginImpl[] origins = {origin};
        return new CacheEvent(attr, origins, origins[0]);
    }

    /**
     * @return 18 events evenly spaced in time from the 1st of January 2001 to
     *         the 31st of that month and evenly spaced over the entire globe
     */
    public static CacheEvent[] createEventTimeRange() {
        Instant t = TimeUtils.parseISOString("20010101T000000.000Z");
        TimeRange tr = new TimeRange(t, Duration.ofDays(30));
        return createEvents(tr, 3, 6);
    }

    public static CacheEvent[] createEvents(TimeRange timeRange,
                                            int rows,
                                            int cols) {
        int numEvents = rows * cols;
        Duration timeBetweenEvents = timeRange.getInterval()
                .dividedBy(numEvents);
        CacheEvent[] events = new CacheEvent[numEvents];
        Location[] locs = MockLocation.create(rows, cols);
        Magnitude[][] mags = MockMagnitude.getMagnitudes(4.0f, 10.0f, numEvents);
        for(int i = 0; i < numEvents; i++) {
            Instant eventBegin = timeRange.getBeginTime()
                    .plus(timeBetweenEvents.multipliedBy(i));
            OriginImpl o = new OriginImpl("Mock Event " + i,
                                      "Mockalog",
                                      "Charlie Groves",
                                      eventBegin,
                                      locs[i],
                                      mags[i],
                                      MockParameterRef.createParams());
            EventAttrImpl ea = MockEventAttr.create();
            events[i] = new CacheEvent(ea, o);
        }
        return events;
    }
}
