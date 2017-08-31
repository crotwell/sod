package edu.sc.seis.sod.process.waveform.vector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;

import org.junit.Test;

import edu.sc.seis.seisFile.fdsnws.stationxml.BaseNodeType;
import edu.sc.seis.sod.mock.seismogram.MockSeismogram;
import edu.sc.seis.sod.mock.station.MockChannelId;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import junit.framework.TestCase;



public class SampleSyncronizeTest extends TestCase {

    public void setUp() {
        syncer = new SampleSyncronize();
    }
    
    @Test
    public void testOnSeismogramsWithSlightlyVaryingStart()
            throws FissuresException, ParseException {
        String[] seisTimes = new String[] { "2003.01.06 23:50:07.481",
                                            "2003.01.06 23:50:07.482",
                                            "2003.01.06 23:50:07.483"};
        LocalSeismogramImpl[] vector = makeSeis(seisTimes);
        LocalSeismogramImpl[] afterShift = new LocalSeismogramImpl[] {SampleSyncronize.alignTimes(vector[0], vector[0]),
                                                                      SampleSyncronize.alignTimes(vector[0], vector[1]),
                                                                      SampleSyncronize.alignTimes(vector[0], vector[2])};
        for (int j = 0; j < afterShift.length; j++) {
            assertEquals("num points", afterShift[0].num_points, afterShift[j].num_points);
            assertEquals("begin time", afterShift[0].getBeginTime(), afterShift[j].getBeginTime());
        }
    }
    
    @Test
    public void testOnSeismogramsWithWidelyVaryingStart()
            throws FissuresException, ParseException {
        String[] seisTimes = new String[] { "2003.01.06 23:50:07.481",
                                            "2003.01.06 23:50:17.482",
                                            "2003.01.06 23:50:27.487"};
        LocalSeismogramImpl[] vector = makeSeis(seisTimes);
        LocalSeismogramImpl[] afterShift = new LocalSeismogramImpl[] {SampleSyncronize.alignTimes(vector[0], vector[0]),
                                                                      SampleSyncronize.alignTimes(vector[0], vector[1]),
                                                                      SampleSyncronize.alignTimes(vector[0], vector[2])};
        for (int j = 0; j < afterShift.length; j++) {
            assertEquals("num points", afterShift[0].num_points, afterShift[j].num_points);
            assertEquals("begin time", Math.IEEEremainder(afterShift[j].getBeginTime().difference(afterShift[0].getBeginTime()).getValue(UnitImpl.SECOND), 
                                                          vector[j].getSampling().getPeriod().getValue(UnitImpl.SECOND)), 0.0, 0.0000001);
        }
    }
    
    LocalSeismogramImpl[] makeSeis(String[] seisTimes) throws ParseException {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        LocalSeismogramImpl[] vector = new LocalSeismogramImpl[seisTimes.length];
        Duration seisWidth = Duration.ofMinutes(10);
        for(int i = 0; i < seisTimes.length; i++) {
            Instant start = BaseNodeType.parseISOString(seisTimes[i]);
            vector[i] = MockSeismogram.createRaggedSpike(start,
                                                            seisWidth,
                                                            20,
                                                            0,
                                                            MockChannelId.createVerticalChanId(),
                                                            20);
        }
        return vector;
    }
    
    SampleSyncronize syncer;
}
