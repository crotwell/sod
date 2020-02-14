package edu.sc.seis.sod.process.waveform.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.mock.seismogram.MockSeismogram;
import edu.sc.seis.sod.mock.station.MockChannelId;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;



public class SampleSynchronizeTest  {

	@BeforeEach
    public void setUp() {
        syncer = new SampleSynchronize();
    }
    
    @Test
    public void testOnSeismogramsWithSlightlyVaryingStart()
            throws FissuresException, ParseException {
        String[] seisTimes = new String[] { "2003-01-06T23:50:07.481",
                                            "2003-01-06T23:50:07.482",
                                            "2003-01-06T23:50:07.483"};
        LocalSeismogramImpl[][] vector = makeSeis(seisTimes);
        SampleSynchronize sampleSyncronize = new SampleSynchronize();
        WaveformVectorResult result =  sampleSyncronize.accept(vector);
        LocalSeismogramImpl[][] afterShift = result.getSeismograms();
        for (int j = 0; j < afterShift.length; j++) {
            assertEquals(afterShift[0][0].getNumPoints(), afterShift[j][0].getNumPoints());
            assertEquals( afterShift[0][0].getBeginTime(), afterShift[j][0].getBeginTime());
        }
    }
    
    @Test
    public void testOnSeismogramsWithWidelyVaryingStart()
            throws FissuresException, ParseException {
        String[] seisTimes = new String[] { "2003-01-06T23:50:07.481",
                                            "2003-01-06T23:50:17.482",
                                            "2003-01-06T23:50:27.487"};
        LocalSeismogramImpl[][] vector = makeSeis(seisTimes);
        SampleSynchronize sampleSyncronize = new SampleSynchronize();
        WaveformVectorResult result =  sampleSyncronize.accept(vector);
        LocalSeismogramImpl[][] afterShift = result.getSeismograms();
        for (int j = 0; j < afterShift.length; j++) {
            assertEquals( afterShift[0][0].getNumPoints(), afterShift[j][0].getNumPoints());
            assertEquals( Math.IEEEremainder(TimeUtils.durationToDoubleSeconds(Duration.between(afterShift[0][0].getBeginTime(), afterShift[j][0].getBeginTime())), 
                                                          TimeUtils.durationToDoubleSeconds(vector[j][0].getSampling().getPeriod())), 0.0, 0.0000001);
        }
    }
    
    LocalSeismogramImpl[][] makeSeis(String[] seisTimes) throws ParseException {
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
        return vector;
    }
    
    SampleSynchronize syncer;
}
