package edu.sc.seis.sod.process.waveform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SeismicPhase;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.mock.MockLocation;
import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.mock.seismogram.MockSeismogram;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.util.convert.sac.FissuresToSac;
import edu.sc.seis.sod.util.display.EventUtil;

public class SacWriterTest  {

	@BeforeEach
    public void setUp() throws CodecException {
        sts = FissuresToSac.getSAC(seis, chan, EventUtil.extractOrigin(ev));
    }

    @Test
    public void testGenerate() throws ConfigurationException {
        String[][] templateAndResult = new String[][] { {"${seismogram.name}.sac",
                                                         seis.getName() + ".sac"},
                                                       {"/${event.catalog}",
                                                        "/" + EventUtil.extractOrigin(ev).getCatalog()},
                                                       {"${channel.name}", ChannelIdUtil.toStringNoDates(chan)}};
        for(int i = 0; i < templateAndResult.length; i++) {
            assertEquals(FissuresFormatter.filize(templateAndResult[i][1]),
                         new SacWriter("", templateAndResult[i][0]).generate(ev, chan, seis, 0, 1));
        }
        assertEquals(FissuresFormatter.filize("test/" + seis.getName()),
                     new SacWriter("test/", seis.getName()).generate(ev, chan, seis, 0, 1));
        assertEquals(FissuresFormatter.filize("test/" + seis.getName()),
                     new SacWriter("test", seis.getName()).generate(ev, chan, seis, 0, 1));
    }

    @Test
    public void testApplyProcessorsWithNoProcessors() throws Exception {
        new SacWriter().applyProcessors(sts, ev, chan);
    }

    @Test
    public void testApplyPhaseHeaderProcessor() throws Exception {
        ArrayList<SacProcess> processes = new ArrayList<SacProcess>();
        processes.add(new SacProcess() {
            @Override
            public void process(SacTimeSeries sac, CacheEvent event, Channel channel) throws Exception {
                sac.getHeader().setKt0("ttp");
            }
        });
        SacWriter sw = new SacWriter(SacWriter.DEFAULT_WORKING_DIR,
                                     SacWriter.DEFAULT_FILE_TEMPLATE,
                                     SacWriter.DEFAULT_PREFIX,
                                     processes,
                                     false,
                                     false);
        sw.applyProcessors(sts, ev, chan);
        assertEquals(sts.getHeader().getKt0(), "ttp");
    }

    @Test
    public void testSecondArrival() throws Exception {
        Location staLoc = Location.of(chan.getStation());
        Location evtLoc = ev.get_preferred_origin().getLocation();
        float evDepth = (float)((QuantityImpl)evtLoc.depth).getValue(UnitImpl.KILOMETER);
        SeismicPhase sp = new SeismicPhase("P", "prem", evDepth);
        DistAz distAz = new DistAz(staLoc, evtLoc);
        double distDeg = distAz.getDelta();
        List<Arrival> arrivals = sp.calcTime(distDeg);
        assertTrue(2 <= arrivals.size());

        ArrayList<SacProcess> processes = new ArrayList<SacProcess>();
        processes.add(new PhaseHeaderProcess("prem", "P", 1, 2));
        SacWriter sw = new SacWriter(SacWriter.DEFAULT_WORKING_DIR,
                                     SacWriter.DEFAULT_FILE_TEMPLATE,
                                     SacWriter.DEFAULT_PREFIX,
                                     processes,
                                     false,
                                     false);
        sw.applyProcessors(sts, ev, chan);
        assertEquals(sts.getHeader().getT1(), (float)arrivals.get(1).getTime());
    }

    @Test
    public void testGenerateLocations() throws ConfigurationException {
        SacWriter sw = new SacWriter("${seismogram.name}.sac");
        assertTrue( sw.generate(ev, chan, seis, 0, 2).endsWith("1.sac"), sw.generate(ev, chan, seis, 0, 2)+" endsWith 1.sac");
        assertTrue( sw.generate(ev, chan, seis, 1, 2).endsWith("2.sac"), sw.generate(ev, chan, seis, 1, 2)+" ends with 2.sac");
    }

    @Test
    public void testRemoveExisting() throws FileNotFoundException, IOException,
            ConfigurationException {
        SacWriter sw = new SacWriter("", System.getProperty("java.io.tmpdir") + File.separator
                + "blahblah");
        String[] locs = new String[5];
        for(int i = 0; i < locs.length; i++) {
            locs[i] = sw.generate(ev, chan, seis, i, locs.length);
            new FileOutputStream(locs[i]).close();
            assertTrue(new File(locs[i]).exists());
        }
        sw.removeExisting(ev, chan, seis, locs.length);
        for(int i = 0; i < locs.length; i++) {
            assertFalse(new File(locs[i]).exists());
        }
    }

    @Test
    public void testIndexAppending() throws FileNotFoundException, IOException,
            ConfigurationException {
        assertEquals("harar${index}", new SacWriter("", "harar").getTemplate());
        assertEquals("$index/harar",new SacWriter("", "$index/harar").getTemplate());
        assertEquals("ha${index}rar",new SacWriter("", "ha${index}rar").getTemplate());
    }

    CacheEvent ev = MockEventAccessOperations.createEvent();

    Channel chan = MockChannel.createChannel(MockLocation.create(10.0f, 10.0f));

    LocalSeismogramImpl seis = MockSeismogram.createSpike(ChannelId.of(chan));

    SacTimeSeries sts;
}
