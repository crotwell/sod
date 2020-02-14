package edu.sc.seis.sod.subsetter.availableData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.mock.station.MockChannelId;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.util.time.ClockUtil;
import junit.framework.TestCase;

public class PercentCoverageTest  {

	@Test
    public void testFullCoverage() {
        PercentCoverage pc = new PercentCoverage(100);
        Instant end = ClockUtil.now();
        Instant start = end.minus(tenMinutes);
        RequestFilter fullTime = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                   start,
                                                   end);
        RequestFilter[] fullArr = new RequestFilter[] {fullTime};
        assertTrue(pc.accept(fullArr, fullArr));
        assertFalse(pc.accept(fullArr, new RequestFilter[0]));
        RequestFilter biggerThanFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.minus(tenMinutes),
                                                         end);
        assertTrue(pc.accept(fullArr, new RequestFilter[] {biggerThanFull}));
    }

	@Test
    public void testHalfCoverage() {
        PercentCoverage pc = new PercentCoverage(50);
        Instant end = ClockUtil.now();
        Instant start = end.minus(tenMinutes);
        RequestFilter fullTime = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                   start,
                                                   end);
        RequestFilter[] fullArr = new RequestFilter[] {fullTime};
        assertTrue(pc.accept(fullArr, fullArr));
        assertFalse(pc.accept(fullArr, new RequestFilter[0]));
        RequestFilter biggerThanFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.minus(tenMinutes),
                                                         end);
        assertTrue(pc.accept(fullArr, new RequestFilter[] {biggerThanFull}));
        RequestFilter halfFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.plus(Duration.ofMinutes(2)),
                                                         end.minus(Duration.ofMinutes(3)));
        assertTrue(pc.accept(fullArr, new RequestFilter[] {halfFull}));
    }

    Duration tenMinutes = Duration.ofMinutes(10);
}
