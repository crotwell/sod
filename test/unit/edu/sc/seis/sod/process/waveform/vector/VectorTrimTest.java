package edu.sc.seis.sod.process.waveform.vector;

import junit.framework.TestCase;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Cut;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

public class VectorTrimTest extends TestCase {

    public void setUp() {
        trimmer = new VectorTrim();
        baseTime = ClockUtil.now();
        baseSeis = createSpike();
    }

    public void testOnThreeEqualSeismograms() throws FissuresException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {baseSeis},
                                                                      {baseSeis},
                                                                      {baseSeis}};
        checkCut(vector);
        checkTrim(trimmer.trim(vector));
    }

    private void checkCut(LocalSeismogramImpl[][] vector) {
        checkCut(vector, baseTime, baseSeis.getEndTime());
    }

    private void checkCut(LocalSeismogramImpl[][] vector,
                          MicroSecondDate begin,
                          MicroSecondDate end) {
        Cut[] cuts = trimmer.findSmallestCoveringCuts(vector);
        assertEquals(1, cuts.length);
        assertEquals(begin, cuts[0].getBegin());
        assertEquals(end, cuts[0].getEnd());
    }

    public void testOnThreeSeismogramsWithIncreasingLength()
            throws FissuresException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {baseSeis},
                                                                      {createSpike(2)},
                                                                      {createSpike(3)}};
        checkCut(vector);
        checkTrim(trimmer.trim(vector));
    }

    private void checkTrim(LocalSeismogramImpl[][] trimmed) {
        checkTrim(trimmed, new MicroSecondTimeRange(baseSeis));
    }

    private void checkTrim(LocalSeismogramImpl[][] trimmed,
                           MicroSecondTimeRange cutTime) {
        for(int i = 0; i < trimmed.length; i++) {
            assertEquals(1, trimmed[i].length);
            assertEquals(cutTime, new MicroSecondTimeRange(trimmed[i][0]));
        }
    }

    public void testOnThreeSeismogramsWithEqualLengthAndIncreasingStartTimes()
            throws FissuresException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {baseSeis},
                                                                      {createSpike(1,
                                                                                   1)},
                                                                      {createSpike(2,
                                                                                   2)}};
        checkEmptyTrim(vector);
    }

    private void checkEmptyTrim(LocalSeismogramImpl[][] vector)
            throws FissuresException {
        LocalSeismogramImpl[][] result = trimmer.trim(vector);
        for(int i = 0; i < result.length; i++) {
            assertEquals(0, result[i].length);
        }
    }

    public void testOnThreeSeismogramsWithIncreasingStartTimesAndDecreasingLength()
            throws FissuresException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {createSpike(3,
                                                                                    0)},
                                                                      {createSpike(2,
                                                                                   1)},
                                                                      {createSpike(1,
                                                                                   2)}};
        checkCut(vector, vector[2][0].getBeginTime(), vector[2][0].getEndTime());
        checkTrim(trimmer.trim(vector), new MicroSecondTimeRange(vector[2][0]));
    }

    public void testOnTwoEqualSeismogramsAndOneMissingSeismogram()
            throws FissuresException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {baseSeis},
                                                                      {},
                                                                      {baseSeis}};
        checkEmptyTrim(trimmer.trim(vector));
    }

    public void testOnSegmentedVectorPieces() throws FissuresException {
        LocalSeismogramImpl[] segmented = new LocalSeismogramImpl[] {createSpike(),
                                                                     createSpike(1,
                                                                                 2)};
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] {segmented,
                                                                      segmented,
                                                                      segmented};
        assertEquals(2, trimmer.findSmallestCoveringCuts(vector).length);
        LocalSeismogramImpl[][] result = trimmer.trim(vector);
        assertEquals(3, result.length);
        for(int i = 0; i < result.length; i++) {
            assertEquals(2, result[i].length);
            for(int j = 0; j < result[i].length; j++) {
                assertEquals(new MicroSecondTimeRange(vector[i][j]),
                             new MicroSecondTimeRange(result[i][j]));
            }
        }
    }

    public void testOnSegmentedUnequalButOverlappingVectorPieces()
            throws FissuresException {
        LocalSeismogramImpl commonSeis = createSpike(2, 2);
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {createSpike(),
                                                                        commonSeis},
                                                                      {createSpike(),
                                                                       createSpike(4,
                                                                                   2)},
                                                                      {createSpike(4,
                                                                                   2)}};
        LocalSeismogramImpl[][] result = trimmer.trim(vector);
        assertEquals(3, result.length);
        for(int i = 0; i < result.length; i++) {
            assertEquals(1, result[i].length);
            for(int j = 0; j < result[i].length; j++) {
                assertEquals(new MicroSecondTimeRange(commonSeis),
                             new MicroSecondTimeRange(result[i][j]));
            }
        }
    }

    public void testOnSegmentedUnoverlappingVectorPieces()
            throws FissuresException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {createSpike(),
                                                                        createSpike(2,
                                                                                    2)},
                                                                      {createSpike(1,
                                                                                   1),
                                                                       createSpike(5,
                                                                                   5)},
                                                                      {createSpike(10,
                                                                                   15)}};
        LocalSeismogramImpl[][] result = trimmer.trim(vector);
        assertEquals(3, result.length);
        for(int i = 0; i < result.length; i++) {
            assertEquals(0, result[i].length);
        }
    }

    private LocalSeismogramImpl createSpike() {
        return createSpike(1);
    }

    private LocalSeismogramImpl createSpike(int mins) {
        return createSpike(mins, 0);
    }

    private LocalSeismogramImpl createSpike(int mins, int minsPastStart) {
        return SimplePlotUtil.createSpike(baseTime.add(new TimeInterval(minsPastStart,
                                                                        UnitImpl.MINUTE)),
                                          new TimeInterval(mins,
                                                           UnitImpl.MINUTE));
    }

    private VectorTrim trimmer;

    private LocalSeismogramImpl baseSeis;

    private MicroSecondDate baseTime;
}
