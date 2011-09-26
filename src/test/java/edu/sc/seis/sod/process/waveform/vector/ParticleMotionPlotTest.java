package edu.sc.seis.sod.process.waveform.vector;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Assert;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;


public class ParticleMotionPlotTest {

    @Test
    public void test() throws Exception {
        CacheEvent event = new CacheEvent(MockEventAccessOperations.createEvent());
        ChannelGroup cg = new ChannelGroup(MockChannel.createMotionVector());
        LocalSeismogramImpl[][] seismograms = new LocalSeismogramImpl[3][1];
        seismograms[0][0] = MockSeismogram.createSpike(event.getPreferred().getTime(), new TimeInterval(600, UnitImpl.SECOND), 10, cg.getChannel1().getId());
        seismograms[1][0] = MockSeismogram.createSpike(event.getPreferred().getTime(), new TimeInterval(600, UnitImpl.SECOND), 10, cg.getChannel2().getId());
        seismograms[2][0] = MockSeismogram.createSpike(event.getPreferred().getTime(), new TimeInterval(600, UnitImpl.SECOND), 10, cg.getChannel3().getId());
        System.out.println("seis "+seismograms[0][0].getBeginTime()+" "+seismograms[0][0].getEndTime()+"  "+event);
        ParticleMotionPlot pmp = new ParticleMotionPlot("test", "Test", "test");
        System.out.println("Test out");
        WaveformVectorResult result = pmp.accept(event,
                   cg,
                   new RequestFilter[3][0], new RequestFilter[3][0],
                   seismograms, null);
        Assert.isTrue(result.isSuccess(), result.getReason().toString());
    }
}
