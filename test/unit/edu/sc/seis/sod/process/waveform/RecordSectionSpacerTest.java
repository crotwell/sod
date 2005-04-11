package edu.sc.seis.sod.process.waveform;

import junit.framework.TestCase;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
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

    private static final AuditInfo[] NO_AUDIT = new AuditInfo[0];

    public void testNoSeis() {
        DataSetSeismogram[] empty = new DataSetSeismogram[0];
        assertEquals(empty.length, spacer.spaceOut(empty).length);
    }

    public void testOneSeis() {
        DataSetSeismogram[] seis = create(new Channel[] {MockChannel.createChannel()});
        assertEquals(seis.length, spacer.spaceOut(seis).length);
    }

    public void testTwoSeis() {
        DataSetSeismogram[] seis = create(new Channel[] {MockChannel.createChannel(),
                                                         MockChannel.createOtherNetChan()});
        assertEquals(seis.length, spacer.spaceOut(seis).length);
    }

    public void testSimpleCluster() {
        DataSetSeismogram[] seis = create(new Channel[] {MockChannel.createChannel(),
                                                         MockChannel.createChannel()});
        assertEquals(1, spacer.spaceOut(seis).length);
    }

    public void testDoubleCluster() {
        DataSetSeismogram[] seis = create(new Channel[] {MockChannel.createChannel(),
                                                         MockChannel.createOtherNetChan(),
                                                         MockChannel.createOtherNetChan(),
                                                         MockChannel.createChannel()});
        assertEquals(2, spacer.spaceOut(seis).length);
    }

    public void testEvenlySpacedLine() {
        for(int i = 30; i < 90; i++) {
            Location[] locs = MockLocation.create(1, i, 0, 0, 0, 180);
            Channel[] chans = MockChannel.createChannelsAtLocs(locs);
            DataSetSeismogram[] seis = create(chans);
            int results = spacer.spaceOut(seis).length;
            if(results < 15 || results > 21) {
                assertTrue("There were "
                                   + results
                                   + " seismograms with "
                                   + i
                                   + " given to it in the spacers results but only 15 through 21 were requested",
                           false);
            } else {
                assertTrue(true);
            }
        }
    }

    public DataSetSeismogram[] create(Channel[] chans) {
        DataSetSeismogram[] seis = new DataSetSeismogram[chans.length];
        MemoryDataSet ds = new MemoryDataSet("Test Id",
                                             "Test Data Set",
                                             "The Tester",
                                             NO_AUDIT);
        ds.addParameter(StdDataSetParamNames.EVENT,
                        MockEventAccessOperations.createEvent(),
                        NO_AUDIT);
        for(int i = 0; i < chans.length; i++) {
            Channel chan = chans[i];
            String chanIdStr = ChannelIdUtil.toString(chan.get_id());
            ds.addParameter(StdDataSetParamNames.CHANNEL + chanIdStr,
                            chan,
                            NO_AUDIT);
            RequestFilter rf = new RequestFilter();
            rf.channel_id = chan.get_id();
            DataSetSeismogram dss = new MemoryDataSetSeismogram(rf, chanIdStr);
            ds.addDataSetSeismogram(dss, NO_AUDIT);
            seis[i] = dss;
        }
        return seis;
    }

    RecordSectionSpacer spacer = new RecordSectionSpacer();
}
