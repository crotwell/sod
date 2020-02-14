package edu.sc.seis.sod.source.seismogram;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.util.time.ClockUtil;

public class WinstonWaveServerTest  {

    @Test 
    @Disabled("no longer run winston on eeyore")
    public void testSeismograms() throws Exception {
        WinstonWaveServer wws = new WinstonWaveServer("eeyore.seis.sc.edu", 16022);
        //MicroSecondDate requestStart = new MicroSecondDate("2011-08-10T12:34:56Z");
        Instant requestStart = ClockUtil.now().minus(Duration.ofMinutes(10));
        RequestFilter rf = new RequestFilter(new ChannelId("CO",
                                                           "JSC",
                                                           "00",
                                                           "HHZ",
                                                           ClockUtil.wayPast()), 
                                                           requestStart,
                                                           requestStart.plus(Duration.ofMinutes(10)));
        List<RequestFilter> in = new ArrayList<RequestFilter>();
        in.add(rf);
        List<LocalSeismogramImpl> out = wws.getSeismogramSource(null, null, null, null).retrieveData(in); // null
                                                                                                          // doesn't
                                                                                                          // matter
        assertTrue(out.size() > 0, "some data");
        
    }

}
