package edu.sc.seis.sod.subsetter.availableData;

import edu.sc.seis.sod.mock.station.MockChannelId;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import junit.framework.TestCase;

public class PercentCoverageTest extends TestCase {

    public void testFullCoverage() {
        PercentCoverage pc = new PercentCoverage(100);
        MicroSecondDate end = new MicroSecondDate();
        TimeInterval tenMinutes = new TimeInterval(10, UnitImpl.MINUTE);
        MicroSecondDate start = end.subtract(tenMinutes);
        RequestFilter fullTime = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                   start,
                                                   end);
        RequestFilter[] fullArr = new RequestFilter[] {fullTime};
        assertTrue(pc.accept(fullArr, fullArr));
        assertFalse(pc.accept(fullArr, new RequestFilter[0]));
        RequestFilter biggerThanFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.subtract(tenMinutes),
                                                         end);
        assertTrue(pc.accept(fullArr, new RequestFilter[] {biggerThanFull}));
    }

    public void testHalfCoverage() {
        PercentCoverage pc = new PercentCoverage(50);
        MicroSecondDate end = new MicroSecondDate();
        TimeInterval tenMinutes = new TimeInterval(10, UnitImpl.MINUTE);
        MicroSecondDate start = end.subtract(tenMinutes);
        RequestFilter fullTime = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                   start,
                                                   end);
        RequestFilter[] fullArr = new RequestFilter[] {fullTime};
        assertTrue(pc.accept(fullArr, fullArr));
        assertFalse(pc.accept(fullArr, new RequestFilter[0]));
        RequestFilter biggerThanFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.subtract(tenMinutes),
                                                         end);
        assertTrue(pc.accept(fullArr, new RequestFilter[] {biggerThanFull}));
        RequestFilter halfFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.add(new TimeInterval(2, UnitImpl.MINUTE)),
                                                         end.subtract(new TimeInterval(3, UnitImpl.MINUTE)));
        assertTrue(pc.accept(fullArr, new RequestFilter[] {halfFull}));
    }
}
