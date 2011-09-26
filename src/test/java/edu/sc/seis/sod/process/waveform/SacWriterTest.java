package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SeismicPhase;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.FissuresFormatter;

public class SacWriterTest extends TestCase {

    public void setUp() throws CodecException {
        sts = FissuresToSac.getSAC(seis, chan, EventUtil.extractOrigin(ev));
    }

    public void testGenerate() throws ConfigurationException {
        String[][] templateAndResult = new String[][] { {"${seismogram.name}.sac",
                                                         seis.getName() + ".sac"},
                                                       {"/${event.catalog}",
                                                        "/" + EventUtil.extractOrigin(ev).getCatalog()},
                                                       {"${channel.name}", chan.getName()}};
        for(int i = 0; i < templateAndResult.length; i++) {
            assertEquals(FissuresFormatter.filize(templateAndResult[i][1]),
                         new SacWriter("", templateAndResult[i][0]).generate(ev, chan, seis, 0));
        }
        assertEquals(FissuresFormatter.filize("test/" + seis.getName()),
                     new SacWriter("test/", seis.getName()).generate(ev, chan, seis, 0));
        assertEquals(FissuresFormatter.filize("test/" + seis.getName()),
                     new SacWriter("test", seis.getName()).generate(ev, chan, seis, 0));
    }

    public void testApplyProcessorsWithNoProcessors() throws Exception {
        new SacWriter().applyProcessors(sts, ev, chan);
    }

    public void testApplyPhaseHeaderProcessor() throws Exception {
        ArrayList<SacProcess> processes = new ArrayList<SacProcess>();
        processes.add(new SacProcess() {
            @Override
            public void process(SacTimeSeries sac, CacheEvent event, ChannelImpl channel) throws Exception {
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
    
    public void testSecondArrival() throws Exception {
        Location staLoc = chan.getStation().getLocation();
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

    public void testGenerateLocations() throws ConfigurationException {
        SacWriter sw = new SacWriter("${seismogram.name}.sac");
        assertTrue(sw.generate(ev, chan, seis, 1).endsWith("1.sac"));
    }

    public void testRemoveExisting() throws FileNotFoundException, IOException,
            ConfigurationException {
        SacWriter sw = new SacWriter("", System.getProperty("java.io.tmpdir") + File.separator
                + "blahblah");
        String[] locs = new String[5];
        for(int i = 0; i < locs.length; i++) {
            locs[i] = sw.generate(ev, chan, seis, i);
            new FileOutputStream(locs[i]).close();
            assertTrue(new File(locs[i]).exists());
        }
        sw.removeExisting(ev, chan, seis);
        for(int i = 0; i < locs.length; i++) {
            assertFalse(new File(locs[i]).exists());
        }
    }

    public void testIndexAppending() throws FileNotFoundException, IOException,
            ConfigurationException {
        assertEquals("harar${index}", new SacWriter("", "harar").getTemplate());
        assertEquals("$index/harar",new SacWriter("", "$index/harar").getTemplate());
        assertEquals("ha${index}rar",new SacWriter("", "ha${index}rar").getTemplate());
    }

    CacheEvent ev = MockEventAccessOperations.createEvent();

    ChannelImpl chan = MockChannel.createChannel(MockLocation.create(10.0f, 10.0f));

    LocalSeismogramImpl seis = MockSeismogram.createSpike(chan.getId());

    SacTimeSeries sts;
}
