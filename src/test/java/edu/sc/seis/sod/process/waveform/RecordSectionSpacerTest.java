package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import junit.framework.TestCase;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.mockFissures.Defaults;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames;

/**
 * @author groves Created on Apr 8, 2005
 */
public class RecordSectionSpacerTest extends TestCase {
    static {BasicConfigurator.configure();}
    
    private static final AuditInfo[] NO_AUDIT = new AuditInfo[0];

    public void testNoSeis() {
        List<DataSetSeismogram> empty = new ArrayList<DataSetSeismogram>();
        assertEquals(empty.size(), spacer.spaceOut(empty).size());
    }

    public void testOneSeis() {
        List<DataSetSeismogram> seis = create(new Channel[] {MockChannel.createChannel()});
        assertEquals(seis.size(), spacer.spaceOut(seis).size());
    }

    public void testTwoSeis() {
        List<DataSetSeismogram> seis = create(new Channel[] {MockChannel.createChannel(),
                                                             MockChannel.createOtherNetChan()});
        assertEquals(seis.size(), spacer.spaceOut(seis).size());
    }

    public void testSimpleCluster() {
        List<DataSetSeismogram> seis = create(new Channel[] {MockChannel.createChannel(), MockChannel.createChannel()});
        assertEquals(1, spacer.spaceOut(seis).size());
    }

    public void testDoubleCluster() {
        List<DataSetSeismogram> seis = create(new Channel[] {MockChannel.createChannel(),
                                                             MockChannel.createOtherNetChan(),
                                                             MockChannel.createOtherNetChan(),
                                                             MockChannel.createChannel()});
        assertEquals(2, spacer.spaceOut(seis).size());
    }

    public void testEvenlySpacedLine() {
        for (int i = 30; i < 90; i++) {
            Location[] locs = MockLocation.create(1, i, 0, 0, 0, 180);
            Channel[] chans = MockChannel.createChannelsAtLocs(locs);
            List<DataSetSeismogram> seis = create(chans);
            int results = spacer.spaceOut(seis).size();
            int fewest = (int)Math.floor(180/spacer.getIdealDegreesBetweenSeis())-1;
            int most = (int)Math.ceil(180/spacer.getMinimumDegreesBetweenSeis())+1;
            if (results < fewest || results > most) {
                assertTrue("There were " + results + " seismograms with " + i
                        + " given to it in the spacers results but only "+fewest+" through "+most+" were requested", false);
            } else {
                assertTrue(true);
            }
        }
    }

    public void testThreeCloseStations() {
        Location[] locs = new Location[] {MockLocation.create(-29.0110, -70.7004), //IU.LCO
                                          MockLocation.create(-64.0489, -64.7744), //IU.PMSA
                                          MockLocation.create(42.5064, -71.5583),  //IU.HRV
                                        //  MockLocation.create(41.6069, -111.5652), //US.HWUT
                                          MockLocation.create(42.7667, -109.5583), // US.BW06
                                          MockLocation.create(42.7654, -111.1004)}; //US.AHID
        CacheEvent event = MockEventAccessOperations.createEvent(Defaults.WALL_FALL, -33.8f, -72.07f);
        Channel[] chans = MockChannel.createChannelsAtLocs(locs);
        List<DataSetSeismogram> seis = create(chans, event);
        List<DataSetSeismogram> best = spacer.spaceOut(seis);
        for (DataSetSeismogram curr : best) {
            System.out.println(""+DisplayUtils.calculateDistance(curr));
        }
        assertEquals("There were " + best.size() + " seismograms with " + locs.length
                + " given to it in the spacers results but 4 should be in the result.", 4, best.size());
    }
    
    public void testMindinao() {
       Location[] locs = new Location[] {MockLocation.create(1.47, 110.31), //MY.  KSM
                                          MockLocation.create(26.84, 128.27), //JP.  JOW
                                          MockLocation.create(7.07, 125.58),  //IU.  DAV
                                          MockLocation.create(24.17, 121.59), //TW. NACB
                                          MockLocation.create(24.46, 118.39) // TW. KMNB
                                          }; //US.AHID
        CacheEvent event = MockEventAccessOperations.createEvent(Defaults.WALL_FALL, 7.4f, 126.44f);
        Channel[] chans = MockChannel.createChannelsAtLocs(locs);
        List<DataSetSeismogram> seis = create(chans, event);
        List<DataSetSeismogram> best = spacer.spaceOut(seis);
        for (DataSetSeismogram curr : best) {
            System.out.println(""+DisplayUtils.calculateDistance(curr));
        }
        assertEquals("There were " + best.size() + " seismograms with " + locs.length
                + " given to it in the spacers results but 2 should be in the result.", 2, best.size());
    }

    public List<DataSetSeismogram> create(Channel[] chans) {
        return create(chans, MockEventAccessOperations.createEvent());
    }
    
    public List<DataSetSeismogram> create(Channel[] chans, CacheEvent event) {
        List<DataSetSeismogram> seis = new ArrayList<DataSetSeismogram>(chans.length);
        MemoryDataSet ds = new MemoryDataSet("Test Id", "Test Data Set", "The Tester", NO_AUDIT);
        ds.addParameter(StdDataSetParamNames.EVENT, event, NO_AUDIT);
        for (int i = 0; i < chans.length; i++) {
            Channel chan = chans[i];
            String chanIdStr = ChannelIdUtil.toString(chan.get_id());
            ds.addParameter(StdDataSetParamNames.CHANNEL + chanIdStr, chan, NO_AUDIT);
            RequestFilter rf = new RequestFilter();
            rf.channel_id = chan.get_id();
            DataSetSeismogram dss = new MemoryDataSetSeismogram(rf, chanIdStr);
            ds.addDataSetSeismogram(dss, NO_AUDIT);
            seis.add(dss);
        }
        return seis;
    }

    RecordSectionSpacer spacer = new RecordSectionSpacer();
}
