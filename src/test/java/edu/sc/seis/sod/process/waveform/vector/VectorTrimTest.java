package edu.sc.seis.sod.process.waveform.vector;

import java.io.BufferedInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import junit.framework.TestCase;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Cut;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannelId;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;

public class VectorTrimTest extends TestCase {

    public void setUp() {
        trimmer = new VectorTrim();
        //baseTime = ClockUtil.now();
        baseTime = new ISOTime("2010-05-26T16:28:40.059Z").getDate();
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
        assertEquals(begin.subtract(((TimeInterval)vector[0][0].getSampling().getPeriod().divideBy(2))), cuts[0].getBegin());
        assertEquals(end.add(((TimeInterval)vector[0][0].getSampling().getPeriod().divideBy(2))), cuts[0].getEnd());
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
            assertEquals("length", trimmed[0].length, trimmed[i].length);
            assertEquals(1, trimmed[i].length);
            for (int j = 0; j < trimmed[i].length; j++) {
                assertEquals("num points", trimmed[0][j].num_points, trimmed[i][j].num_points);
                assertEquals("begin time", trimmed[0][j].getBeginTime(), trimmed[i][j].getBeginTime());
            }
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
                assertEquals(" "+i+" "+j, new MicroSecondTimeRange(vector[i][j]),
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

    public void testSamplingNormalization() {
        LocalSeismogramImpl seis = MockSeismogram.createSpike();
        LocalSeismogramImpl other = resample(seis, seis.getNumPoints() + 1);
        trimmer.normalizeSampling(new LocalSeismogramImpl[][] { {seis}, {other}});
        assertEquals(seis.getSampling(), other.getSampling());
        other = resample(seis, seis.getNumPoints() * 2);
        assertFalse(seis.getSampling().equals(other.getSampling()));
    }

    private LocalSeismogramImpl resample(LocalSeismogramImpl seis, int numPoints) {
        LocalSeismogramImpl other = new LocalSeismogramImpl(seis,
                                                            new int[numPoints]);
        other.sampling_info = new SamplingImpl(other.getNumPoints(),
                                               seis.getTimeInterval());
        return other;
    }

    public void testOnSeismogramsWithVaryingSamplingALaPond()
            throws FissuresException, ParseException {
        String[][] seisTimes = new String[][] { {"2003.01.06 23:50:07.483",
                                                 "2003.01.07 00:08:08.334"},
                                               {"2003.01.06 23:51:59.483",
                                                "2003.01.07 00:06:18.234"},
                                               {"2003.01.06 23:49:21.983",
                                                "2003.01.07 00:06:16.734"}};
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[seisTimes.length][1];
        for(int i = 0; i < seisTimes.length; i++) {
            MicroSecondDate start = new MicroSecondDate(timeFormat.parse(seisTimes[i][0]));
            MicroSecondDate end = new MicroSecondDate(timeFormat.parse(seisTimes[i][1]));
            vector[i][0] = MockSeismogram.createRaggedSpike(start,
                                                            end.subtract(start),
                                                            20,
                                                            0,
                                                            MockChannelId.createVerticalChanId(),
                                                            20 - i / 133.0);
        }
        trimmer.normalizeSampling(vector);
        MicroSecondDate start = vector[1][0].getBeginTime(); // 1,0 has latest start
        MicroSecondDate end = vector[2][0].getEndTime();     // 2,0 has earliest end
        checkCut(vector, start, end); 
        Cut cut = trimmer.findSmallestCoveringCuts(vector)[0];
        checkTrim(trimmer.trim(vector), new MicroSecondTimeRange(cut.getBegin(), cut.getEnd()));
    }

    public void testOnSeismogramsWithSlightlyVaryingStart()
            throws FissuresException, ParseException {
        String[] seisTimes = new String[] { "2003.01.06 23:50:07.481",
                                            "2003.01.06 23:50:07.482",
                                            "2003.01.06 23:50:07.483"};
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[seisTimes.length][1];
        TimeInterval seisWidth = new TimeInterval(10, UnitImpl.MINUTE);
        for(int i = 0; i < seisTimes.length; i++) {
            MicroSecondDate start = new MicroSecondDate(timeFormat.parse(seisTimes[i]));
            vector[i][0] = MockSeismogram.createRaggedSpike(start,
                                                            seisWidth,
                                                            20,
                                                            0,
                                                            MockChannelId.createVerticalChanId(),
                                                            20);
        }
        trimmer.normalizeSampling(vector);
        MicroSecondDate start = vector[2][0].getBeginTime(); // 2,0 has latest start
        MicroSecondDate end = vector[0][0].getEndTime();     // 0,0 has earliest end
        checkCut(vector, start, end); 
        Cut cut = trimmer.findSmallestCoveringCuts(vector)[0];
        checkTrim(trimmer.trim(vector), new MicroSecondTimeRange(cut.getBegin(), cut.getEnd()));
    }
    
    public void testAmmonData() throws Exception {
        String ammomSacFileBase = "edu/sc/seis/sod/process/waveform/vector/vectorTrimTest/poha-iu-10.lh";
        BufferedInputStream in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ammomSacFileBase+"z"));
        LocalSeismogramImpl z = SacToFissures.getSeismogram(in);
        in.close();
        in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ammomSacFileBase+"n"));
        LocalSeismogramImpl n = SacToFissures.getSeismogram(in);
        in.close();
        in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ammomSacFileBase+"e"));
        LocalSeismogramImpl e = SacToFissures.getSeismogram(in);
        in.close();

        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {z},
                                                                      {n},
                                                                      {e}};
        // e is latest begin, z is earliest end
        checkCut(vector, e.getBeginTime(), z.getEndTime());
        checkTrim(trimmer.trim(vector));
    }

    private LocalSeismogramImpl createSpike() {
        return createSpike(1);
    }

    private LocalSeismogramImpl createSpike(int mins) {
        return createSpike(mins, 0);
    }

    private LocalSeismogramImpl createSpike(int mins, int minsPastStart) {
        return MockSeismogram.createSpike(baseTime.add(new TimeInterval(minsPastStart,
                                                                        UnitImpl.MINUTE)),
                                          new TimeInterval(mins,
                                                           UnitImpl.MINUTE));
    }

    private VectorTrim trimmer;

    private LocalSeismogramImpl baseSeis;

    private MicroSecondDate baseTime;
}
