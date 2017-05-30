package edu.sc.seis.sod.process.waveform.vector;

import org.junit.Test;

import edu.sc.seis.TauP.Assert;
import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.mock.seismogram.MockSeismogram;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.model.common.Time;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelImpl;


public class ParticleMotionPlotTest {

    @Test
    public void test() throws Exception {
        CacheEvent event = new CacheEvent(MockEventAccessOperations.createEvent());
        ChannelGroup cg = new ChannelGroup(MockChannel.createMotionVector());
        ChannelImpl[] horizontal = cg.getHorizontalXY();
        int rot = -10;
        horizontal[0].getOrientation().azimuth += rot;
        horizontal[1].getOrientation().azimuth += rot;
        LocalSeismogramImpl[][] seismograms = new LocalSeismogramImpl[3][1];
/*
        seismograms[0][0] = MockSeismogram.createSpike(event.getPreferred().getTime(), new TimeInterval(600, UnitImpl.SECOND), 10, cg.getChannel1().getId());
        seismograms[1][0] = MockSeismogram.createSpike(event.getPreferred().getTime(), new TimeInterval(600, UnitImpl.SECOND), 10, cg.getChannel2().getId());
        seismograms[2][0] = MockSeismogram.createSpike(event.getPreferred().getTime(), new TimeInterval(600, UnitImpl.SECOND), 10, cg.getChannel3().getId());
*/
        seismograms[0][0] = MockSeismogram.createTestData("one", new int[] { 0, 1, 2, 3, 4}, new Time(event.getPreferred().getTime()), cg.getVertical().getId());
        seismograms[1][0] = MockSeismogram.createTestData("two", new int[] { 0, 1, 1, 1, 1}, new Time(event.getPreferred().getTime()), horizontal[0].getId());
        seismograms[2][0] = MockSeismogram.createTestData("three", new int[] {0, 1, 0, -1, -2}, new Time(event.getPreferred().getTime()), horizontal[1].getId());
        System.out.println("seis "+seismograms[0][0].getBeginTime()+" "+seismograms[0][0].getEndTime()+"  "+event);
        ParticleMotionPlot pmp = new ParticleMotionPlot("build", ParticleMotionPlot.DEFAULT_FILE_TEMPLATE, "test");
        System.out.println("Test out");
        WaveformVectorResult result = pmp.accept(event,
                   cg,
                   new RequestFilter[3][0], new RequestFilter[3][0],
                   seismograms, null);
        Assert.isTrue(result.isSuccess(), result.getReason().toString());
    }
}
