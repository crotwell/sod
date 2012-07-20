package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class WinstonWaveServerTest extends TestCase {

    @Test
    public void testAvailable() throws Exception {
        WinstonWaveServer wws = new WinstonWaveServer("eeyore.seis.sc.edu", 16022);
        RequestFilter rf = new RequestFilter(new ChannelId(new NetworkId("CO", ClockUtil.wayPast().getFissuresTime()),
                                                           "JSC",
                                                           "00",
                                                           "HHZ",
                                                           ClockUtil.wayPast().getFissuresTime()),
                                             ClockUtil.yesterday().getFissuresTime(),
                                             ClockUtil.now().getFissuresTime());
        List<RequestFilter> in = new ArrayList<RequestFilter>();
        in.add(rf);
        List<RequestFilter> out = wws.getSeismogramSource(null, null, null, null).available_data(in); // null
                                                                                                      // doesn't
                                                                                                      // matter
        assertTrue("some data", out.size() > 0);

    }

    @Test
    public void testSeismograms() throws Exception {
        WinstonWaveServer wws = new WinstonWaveServer("eeyore.seis.sc.edu", 16022);
        MicroSecondDate requestStart = new MicroSecondDate("2011-08-10T12:34:56Z");
        RequestFilter rf = new RequestFilter(new ChannelId(new NetworkId("CO", ClockUtil.wayPast().getFissuresTime()),
                                                           "JSC",
                                                           "00",
                                                           "HHZ",
                                                           ClockUtil.wayPast().getFissuresTime()), 
                                                           requestStart.getFissuresTime(),
                                                           requestStart.add(new TimeInterval(10, UnitImpl.MINUTE)).getFissuresTime());
        List<RequestFilter> in = new ArrayList<RequestFilter>();
        in.add(rf);
        List<LocalSeismogramImpl> out = wws.getSeismogramSource(null, null, null, null).retrieveData(in); // null
                                                                                                          // doesn't
                                                                                                          // matter
        assertTrue("some data", out.size() > 0);
        
    }

}
