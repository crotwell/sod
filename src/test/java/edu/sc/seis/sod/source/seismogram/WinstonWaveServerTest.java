package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.NetworkId;
import edu.sc.seis.sod.util.time.ClockUtil;
import junit.framework.TestCase;

public class WinstonWaveServerTest extends TestCase {

    @Test
    public void testAvailable() throws Exception {
        WinstonWaveServer wws = new WinstonWaveServer("eeyore.seis.sc.edu", 16022);
        RequestFilter rf = new RequestFilter(new ChannelId(new NetworkId("CO", ClockUtil.wayPast()),
                                                           "JSC",
                                                           "00",
                                                           "HHZ",
                                                           ClockUtil.wayPast()),
                                             ClockUtil.yesterday(),
                                             ClockUtil.now());
        List<RequestFilter> in = new ArrayList<RequestFilter>();
        in.add(rf);
        List<RequestFilter> out = wws.getSeismogramSource(null, null, null, null).availableData(in); // null
                                                                                                      // doesn't
                                                                                                      // matter
        assertTrue("some data", out.size() > 0);

    }

    @Test
    public void testSeismograms() throws Exception {
        WinstonWaveServer wws = new WinstonWaveServer("eeyore.seis.sc.edu", 16022);
        //MicroSecondDate requestStart = new MicroSecondDate("2011-08-10T12:34:56Z");
        MicroSecondDate requestStart = ClockUtil.now().subtract(new TimeInterval(10, UnitImpl.MINUTE));
        RequestFilter rf = new RequestFilter(new ChannelId(new NetworkId("CO", ClockUtil.wayPast()),
                                                           "JSC",
                                                           "00",
                                                           "HHZ",
                                                           ClockUtil.wayPast()), 
                                                           requestStart,
                                                           requestStart.add(new TimeInterval(10, UnitImpl.MINUTE)));
        List<RequestFilter> in = new ArrayList<RequestFilter>();
        in.add(rf);
        List<LocalSeismogramImpl> out = wws.getSeismogramSource(null, null, null, null).retrieveData(in); // null
                                                                                                          // doesn't
                                                                                                          // matter
        assertTrue("some data", out.size() > 0);
        
    }

}
