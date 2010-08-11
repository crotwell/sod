package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import gov.usgs.earthworm.Menu;
import gov.usgs.earthworm.MenuItem;
import gov.usgs.earthworm.WaveServer;
import gov.usgs.winston.Channel;
import gov.usgs.winston.Instrument;
import gov.usgs.winston.server.WWSClient;

public class WinstonWaveServerTest extends TestCase {

    @Test
    public void testAvailable() throws Exception {
        WinstonWaveServer wws = new WinstonWaveServer("eeyore.seis.sc.edu", 16022);
        RequestFilter rf = new RequestFilter(new ChannelId(new NetworkId("CO", ClockUtil.wayPast().getFissuresTime()),
                                                           "JSC",
                                                           "00",
                                                           "BHZ",
                                                           ClockUtil.wayPast().getFissuresTime()),
                                             ClockUtil.yesterday().getFissuresTime(),
                                             ClockUtil.now().getFissuresTime());
        List<RequestFilter> in = new ArrayList<RequestFilter>();
        in.add(rf);
        List<RequestFilter> out = wws.getSeismogramSource(null, null, null, null).available_data(in); // null
                                                                                                      // doesn't
                                                                                                      // matter
        assertTrue("some data", out.size() > 0);
        for (RequestFilter filter : out) {
            // System.out.println(ChannelIdUtil.toStringNoDates(filter.channel_id)+"  "+filter.start_time.date_time+"  "+filter.end_time.date_time);
        }
    }

    @Test
    public void testSeismograms() throws Exception {
        WinstonWaveServer wws = new WinstonWaveServer("eeyore.seis.sc.edu", 16022);
        RequestFilter rf = new RequestFilter(new ChannelId(new NetworkId("CO", ClockUtil.wayPast().getFissuresTime()),
                                                           "JSC",
                                                           "00",
                                                           "BHZ",
                                                           ClockUtil.wayPast().getFissuresTime()), ClockUtil.now()
                .subtract(new TimeInterval(10, UnitImpl.MINUTE))
                .getFissuresTime(), ClockUtil.now().getFissuresTime());
        List<RequestFilter> in = new ArrayList<RequestFilter>();
        in.add(rf);
        List<LocalSeismogramImpl> out = wws.getSeismogramSource(null, null, null, null).retrieveData(in); // null
                                                                                                          // doesn't
                                                                                                          // matter
        assertTrue("some data", out.size() > 0);
        for (LocalSeismogramImpl seis : out) {
            // System.out.println(ChannelIdUtil.toStringNoDates(seis.channel_id)+"  "+seis.getBeginTime()+"  "+seis.getEndTime()+"  "+seis.getNumPoints()+"  "+seis.getSampling());
        }
    }

    @Test
    public void testChannel() throws Exception {
        String host = "localhost";
        // "pubwave.ceri.memphis.edu"
        // "pubavo1.wr.usgs.gov", 16022);
        for (int port = 16022; port <= 16022; port++) {
            System.out.println(host+":"+port);
            WinstonWaveServer wws = new WinstonWaveServer(host, port);
            WWSClient directWinston = wws.getWaveServer();
            if (!directWinston.connect()) {
                System.err.println("No connection: "+host+":"+port);
                continue;
            }
            Menu m = directWinston.getMenu();
            List<MenuItem> items = m.getSortedItems();
            for (MenuItem menuItem : items) {
                // System.out.println(menuItem.getNetwork()+"."+menuItem.getStation()+"."+menuItem.getLocation()+"."+menuItem.getChannel());
            }
            List<Channel> chans = directWinston.getChannels(true);
            for (Channel first : chans) {
                System.out.println("Channel: " + first.getAlias() + "  " + first.getCode() + "  "
                        + first.getGroupString() + "  " + first.getLinearA() + "  " + first.getLinearB() + "  "
                        + first.getUnit());
                Instrument inst = first.getInstrument();
                System.out.println("Instrument: " + inst.getDescription() + "  " + inst.getHeight() + "  "
                        + inst.getLatitude() + "  " + inst.getLongitude() + "  " + inst.getName());
            }
        }
    }
}
