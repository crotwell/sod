package edu.sc.seis.sod.subsetter.availableData;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannelId;
import junit.framework.TestCase;

public class PercentCoverageTest extends TestCase {

    public void testFullCoverage() {
        PercentCoverage pc = new PercentCoverage(100);
        MicroSecondDate end = new MicroSecondDate();
        TimeInterval tenMinutes = new TimeInterval(10, UnitImpl.MINUTE);
        MicroSecondDate start = end.subtract(tenMinutes);
        RequestFilter fullTime = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                   start.getFissuresTime(),
                                                   end.getFissuresTime());
        RequestFilter[] fullArr = new RequestFilter[] {fullTime};
        assertTrue(pc.accept(fullArr, fullArr));
        assertFalse(pc.accept(fullArr, new RequestFilter[0]));
        RequestFilter biggerThanFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.subtract(tenMinutes)
                                                                 .getFissuresTime(),
                                                         end.getFissuresTime());
        assertTrue(pc.accept(fullArr, new RequestFilter[] {biggerThanFull}));
    }

    public void testHalfCoverage() {
        PercentCoverage pc = new PercentCoverage(50);
        MicroSecondDate end = new MicroSecondDate();
        TimeInterval tenMinutes = new TimeInterval(10, UnitImpl.MINUTE);
        MicroSecondDate start = end.subtract(tenMinutes);
        RequestFilter fullTime = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                   start.getFissuresTime(),
                                                   end.getFissuresTime());
        RequestFilter[] fullArr = new RequestFilter[] {fullTime};
        assertTrue(pc.accept(fullArr, fullArr));
        assertFalse(pc.accept(fullArr, new RequestFilter[0]));
        RequestFilter biggerThanFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.subtract(tenMinutes)
                                                                 .getFissuresTime(),
                                                         end.getFissuresTime());
        assertTrue(pc.accept(fullArr, new RequestFilter[] {biggerThanFull}));
        RequestFilter halfFull = new RequestFilter(MockChannelId.createVerticalChanId(),
                                                         start.add(new TimeInterval(2, UnitImpl.MINUTE))
                                                                 .getFissuresTime(),
                                                         end.subtract(new TimeInterval(3, UnitImpl.MINUTE)).getFissuresTime());
        assertTrue(pc.accept(fullArr, new RequestFilter[] {halfFull}));
    }
}
