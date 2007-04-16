package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.FissuresFormatter;

public class SacWriterTest extends TestCase {

    public void setUp() throws CodecException {
        sts = FissuresToSac.getSAC(seis, chan, EventUtil.extractOrigin(ev));
    }

    static {
        BasicConfigurator.configure(new NullAppender());
    }

    public void testGenerate() throws ConfigurationException {
        String[][] templateAndResult = new String[][] { {"${seismogram.name}.sac",
                                                         seis.getName() + ".sac"},
                                                       {"/${event.catalog}",
                                                        "/" + EventUtil.extractOrigin(ev).catalog},
                                                       {"${channel.name}", chan.name}};
        for(int i = 0; i < templateAndResult.length; i++) {
            assertEquals(FissuresFormatter.filize(templateAndResult[i][1]),
                         new SacWriter("", templateAndResult[i][0]).generate(ev, chan, seis, 0));
        }
        assertEquals(FissuresFormatter.filize("test/" + seis.getName()),
                     new SacWriter("test/", seis.getName()).generate(ev, chan, seis, 0));
        assertEquals(FissuresFormatter.filize("test/" + seis.getName()),
                     new SacWriter("test", seis.getName()).generate(ev, chan, seis, 0));
    }

    public void testApplyProcessorsWithNoProcessors() throws CodecException, ConfigurationException {
        new SacWriter().applyProcessors(sts, ev, chan);
    }

    public void testApplyPhaseHeaderProcessor() throws ConfigurationException {
        SacWriter sw = new SacWriter(new SacProcess[] {new SacProcess() {

            public void process(SacTimeSeries sac, EventAccessOperations event, Channel channel) {
                sac.kt0 = "ttp";
            }
        }});
        sw.applyProcessors(sts, ev, chan);
        assertEquals(sts.kt0, "ttp");
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

    EventAccessOperations ev = MockEventAccessOperations.createEvent();

    Channel chan = MockChannel.createChannel();

    LocalSeismogramImpl seis = SimplePlotUtil.createSpike();

    SacTimeSeries sts;
}
