package edu.sc.seis.sod.process.waveform.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.BufferedInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.bag.Cut;
import edu.sc.seis.sod.mock.seismogram.MockSeismogram;
import edu.sc.seis.sod.mock.station.MockChannelId;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.subsetter.SubsetterException;
import edu.sc.seis.sod.util.convert.sac.SacToFissures;

public class VectorTrimTest  {

	@BeforeEach
    public void setUp() {
        trimmer = new VectorTrim();
        //baseTime = ClockUtil.now();
        baseTime = TimeUtils.parseISOString("2010-05-26T16:28:40.059Z");
        baseSeis = createSpike();
    }

    @Test
    public void testOnThreeEqualSeismograms() throws FissuresException, SubsetterException {
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
                          Instant begin,
                          Instant end) {
        Cut[] cuts = trimmer.findSmallestCoveringCuts(vector);
        assertEquals(1, cuts.length);
        assertEquals(begin.minus((vector[0][0].getSampling().getPeriod().dividedBy(2))), cuts[0].getBegin());
        assertEquals(end.plus((vector[0][0].getSampling().getPeriod().dividedBy(2))), cuts[0].getEnd());
    }

    @Test
    public void testOnThreeSeismogramsWithIncreasingLength()
            throws FissuresException, SubsetterException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {baseSeis},
                                                                      {createSpike(2)},
                                                                      {createSpike(3)}};
        checkCut(vector);
        checkTrim(trimmer.trim(vector));
    }

    private void checkTrim(LocalSeismogramImpl[][] trimmed) {
        checkTrim(trimmed, new TimeRange(baseSeis));
    }

    private void checkTrim(LocalSeismogramImpl[][] trimmed,
                           TimeRange cutTime) {
        for(int i = 0; i < trimmed.length; i++) {
            assertEquals( trimmed[0].length, trimmed[i].length);
            assertEquals(1, trimmed[i].length);
            for (int j = 0; j < trimmed[i].length; j++) {
                assertEquals( trimmed[0][j].num_points, trimmed[i][j].num_points);
                assertEquals( trimmed[0][j].getBeginTime(), trimmed[i][j].getBeginTime());
            }
        }
    }

    @Test
    public void testOnThreeSeismogramsWithEqualLengthAndIncreasingStartTimes()
            throws FissuresException, SubsetterException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {baseSeis},
                                                                      {createSpike(1,
                                                                                   1)},
                                                                      {createSpike(2,
                                                                                   2)}};
        checkEmptyTrim(vector);
    }

    private void checkEmptyTrim(LocalSeismogramImpl[][] vector)
            throws FissuresException, SubsetterException {
        LocalSeismogramImpl[][] result = trimmer.trim(vector);
        for(int i = 0; i < result.length; i++) {
            assertEquals(0, result[i].length);
        }
    }

    @Test
    public void testOnThreeSeismogramsWithIncreasingStartTimesAndDecreasingLength()
            throws FissuresException, SubsetterException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {createSpike(3,
                                                                                    0)},
                                                                      {createSpike(2,
                                                                                   1)},
                                                                      {createSpike(1,
                                                                                   2)}};
        checkCut(vector, vector[2][0].getBeginTime(), vector[2][0].getEndTime());
        checkTrim(trimmer.trim(vector), new TimeRange(vector[2][0]));
    }

    @Test
    public void testOnTwoEqualSeismogramsAndOneMissingSeismogram()
            throws FissuresException, SubsetterException {
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[][] { {baseSeis},
                                                                      {},
                                                                      {baseSeis}};
        checkEmptyTrim(trimmer.trim(vector));
    }

    @Test
    public void testOnSegmentedVectorPieces() throws FissuresException, SubsetterException {
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
                assertEquals( new TimeRange(vector[i][j]),
                             new TimeRange(result[i][j]));
            }
        }
    }

    @Test
    public void testOnSegmentedUnequalButOverlappingVectorPieces()
            throws FissuresException, SubsetterException {
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
                assertEquals(new TimeRange(commonSeis),
                             new TimeRange(result[i][j]));
            }
        }
    }

    @Test
    public void testOnSegmentedUnoverlappingVectorPieces()
            throws FissuresException, SubsetterException {
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

    @Test
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

    @Test
    public void testOnSeismogramsWithVaryingSamplingALaPond()
            throws FissuresException, ParseException, SubsetterException {
        String[][] seisTimes = new String[][] { {"2003-01-06T23:50:07.483",
                                                 "2003-01-07T00:08:08.334"},
                                               {"2003-01-06T23:51:59.483",
                                                "2003-01-07T00:06:18.234"},
                                               {"2003-01-06T23:49:21.983",
                                                "2003-01-07T00:06:16.734"}};
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[seisTimes.length][1];
        for(int i = 0; i < seisTimes.length; i++) {
            Instant start = TimeUtils.parseISOString(seisTimes[i][0]);
            Instant end = TimeUtils.parseISOString(seisTimes[i][1]);
            vector[i][0] = MockSeismogram.createRaggedSpike(start,
                                                            Duration.between(start, end),
                                                            20,
                                                            0,
                                                            MockChannelId.createVerticalChanId(),
                                                            20 - i / 133.0);
        }
        trimmer.normalizeSampling(vector);
        Instant start = vector[1][0].getBeginTime(); // 1,0 has latest start
        Instant end = vector[2][0].getEndTime();     // 2,0 has earliest end
        checkCut(vector, start, end); 
        Cut cut = trimmer.findSmallestCoveringCuts(vector)[0];
        checkTrim(trimmer.trim(vector), new TimeRange(cut.getBegin(), cut.getEnd()));
    }

    @Test
    public void testOnSeismogramsWithSlightlyVaryingStart()
            throws FissuresException, ParseException, SubsetterException {
        String[] seisTimes = new String[] { "2003-01-06T23:50:07.481",
                                            "2003-01-06T23:50:07.482",
                                            "2003-01-06T23:50:07.483"};
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        LocalSeismogramImpl[][] vector = new LocalSeismogramImpl[seisTimes.length][1];
        Duration seisWidth = Duration.ofMinutes(10);
        for(int i = 0; i < seisTimes.length; i++) {
            Instant start = TimeUtils.parseISOString(seisTimes[i]);
            vector[i][0] = MockSeismogram.createRaggedSpike(start,
                                                            seisWidth,
                                                            20,
                                                            0,
                                                            MockChannelId.createVerticalChanId(),
                                                            20);
        }
        trimmer.normalizeSampling(vector);
        Instant start = vector[2][0].getBeginTime(); // 2,0 has latest start
        Instant end = vector[0][0].getEndTime();     // 0,0 has earliest end
        checkCut(vector, start, end); 
        Cut cut = trimmer.findSmallestCoveringCuts(vector)[0];
        checkTrim(trimmer.trim(vector), new TimeRange(cut.getBegin(), cut.getEnd()));
    }

    @Test
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
        return MockSeismogram.createSpike(baseTime.plus(Duration.ofMinutes(minsPastStart)),
                                                        Duration.ofMinutes(mins));
    }

    private VectorTrim trimmer;

    private LocalSeismogramImpl baseSeis;

    private Instant baseTime;
}
