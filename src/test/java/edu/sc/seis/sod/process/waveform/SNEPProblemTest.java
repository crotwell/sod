package edu.sc.seis.sod.process.waveform;

import junit.framework.TestCase;
import edu.iris.Fissures.IfSeismogramDC.Property;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

public class SNEPProblemTest extends TestCase {

    public SNEPProblemTest() {
        clean = new LocalSeismogramImpl[] {SimplePlotUtil.createSineWave()};
        noType = new LocalSeismogramImpl[] {SimplePlotUtil.createSineWave()};
        noType[0].properties = new Property[] {new Property("snep.problem", "")};
        typed = new LocalSeismogramImpl[] {SimplePlotUtil.createSineWave()};
        typed[0].properties = new Property[] {new Property("snep.problem",
                                                           "flatline")};
    }

    public void testProcessWithNoType() throws Exception {
        SNEPProblem process = new SNEPProblem();
        WaveformResult result = process.process(null,
                                                null,
                                                null,
                                                null,
                                                clean,
                                                null);
        assertFalse(result.isSuccess());
        result = process.process(null, null, null, null, noType, null);
        assertTrue(result.isSuccess());
        result = process.process(null, null, null, null, typed, null);
        assertTrue(result.isSuccess());
    }

    public void testProcessWithType() throws Exception {
        SNEPProblem process = new SNEPProblem("flatline");
        WaveformResult result = process.process(null,
                                                null,
                                                null,
                                                null,
                                                clean,
                                                null);
        assertFalse(result.isSuccess());
        result = process.process(null, null, null, null, noType, null);
        assertFalse(result.isSuccess());
        result = process.process(null, null, null, null, typed, null);
        assertTrue(result.isSuccess());
    }

    public void testProcessWithWrongType() throws Exception {
        SNEPProblem process = new SNEPProblem("timing");
        WaveformResult result = process.process(null,
                                                null,
                                                null,
                                                null,
                                                clean,
                                                null);
        assertFalse(result.isSuccess());
        result = process.process(null, null, null, null, noType, null);
        assertFalse(result.isSuccess());
        result = process.process(null, null, null, null, typed, null);
        assertFalse(result.isSuccess());
    }

    LocalSeismogramImpl[] clean, noType, typed;
}
