package edu.sc.seis.sod.mock.seismogram;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.mock.station.MockChannelId;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.TimeSeriesDataSel;
import edu.sc.seis.sod.model.station.ChannelId;


public class MockSeismogram {

    public static final int SPIKE_SAMPLES_PER_SECOND = 20;
    
    public static final Duration DEFAULT_TRACE_LENGTH = Duration.ofSeconds(50);

    public static int[] createRandomDataBits(int length) {
        int[] dataBits = new int[length];
        double tmpDouble;
        for(int i = 0; i < dataBits.length; i++) {
            tmpDouble = Math.random() * 2.0 - 1.0;
            // tmpDouble = .4 + Math.random()*.1;
            // this makes the values a little more likely to be close
            // to the center, making it slightly more seimogram like
            tmpDouble = tmpDouble * tmpDouble * tmpDouble * tmpDouble
                    * tmpDouble;
            dataBits[i] = (int)Math.round(tmpDouble * 2000.0);
        }
        return dataBits;
    }

    public static LocalSeismogramImpl createTestData() {
        return createTestData("Fake Data");
    }

    public static LocalSeismogramImpl createTestData(String name) {
        return createTestData(name, createRandomDataBits(100));
    }

    public static LocalSeismogramImpl createTestData(String name, int[] dataBits) {
        return createTestData(name, createBits(dataBits), dataBits.length);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     float[] dataBits) {
        return createTestData(name, createBits(dataBits), dataBits.length);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                      TimeSeriesDataSel bits,
                                                      int bitsLength) {
        Instant time =  TimeUtils.parseISOString("1999-12-31T23:59:59.000Z");
        Duration timeInterval = Duration.ofSeconds(1);
        SamplingImpl sampling = new SamplingImpl(20, timeInterval);
        return createTestData(name,
                              bits,
                              bitsLength,
                              time,
                              MockChannelId.makeChanId(time),
                              sampling);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     int[] dataBits,
                                                     Instant time) {
        return createTestData(name, dataBits, time, MockChannelId.makeChanId(time));
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     int[] dataBits,
                                                     Instant time,
                                                     ChannelId channelID) {
        Duration timeInterval = Duration.ofSeconds(1);
        SamplingImpl sampling = new SamplingImpl(20, timeInterval);
        return createTestData(name, dataBits, time, channelID, sampling);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     int[] dataBits,
                                                     Instant time,
                                                     ChannelId channelID,
                                                     SamplingImpl sampling) {
        return createTestData(name,
                              createBits(dataBits),
                              dataBits.length,
                              time,
                              channelID,
                              sampling);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     float[] dataBits,
                                                     Instant time,
                                                     ChannelId channelID,
                                                     SamplingImpl sampling) {
        return createTestData(name,
                              createBits(dataBits),
                              dataBits.length,
                              time,
                              channelID,
                              sampling);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                      TimeSeriesDataSel bits,
                                                      int bitsLength,
                                                      Instant time,
                                                      ChannelId channelID,
                                                      SamplingImpl sampling) {
        String id = "Nowhere: " + name;
        LocalSeismogramImpl seis = new LocalSeismogramImpl(id,
                                                           time,
                                                           bitsLength,
                                                           sampling,
                                                           UnitImpl.COUNT,
                                                           channelID,
                                                           bits);
        seis.setProperty("Name", name);
        return seis;
    }

    private static TimeSeriesDataSel createBits(int[] dataBits) {
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.int_values(dataBits);
        return bits;
    }

    private static TimeSeriesDataSel createBits(float[] dataBits) {
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.flt_values(dataBits);
        return bits;
    }

    public static LocalSeismogramImpl createCustomSineWave() {
        int[] dataBits = new int[1200];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(0 + i * Math.PI * 1 / 20.0) * 1000);
        }
        return createTestData("Sine Wave",
                              dataBits,
                              TimeUtils.parseISOString("19911015T163000.000Z"));
    }

    public static LocalSeismogramImpl createSineWave() {
        return createSineWave(0);
    }

    public static LocalSeismogramImpl createSineWave(double phase) {
        return createSineWave(phase, 1);
    }

    public static LocalSeismogramImpl createSineWave(double phase, double hertz) {
        return createSineWave(phase, hertz, 1200);
    }

    public static LocalSeismogramImpl createSineWave(double phase,
                                                     double hertz,
                                                     int numPoints) {
        return createSineWave(phase, hertz, numPoints, 1000);
    }

    public static LocalSeismogramImpl createSineWave(double phase,
                                                     double hertz,
                                                     int numPoints,
                                                     double amp) {
        int[] dataBits = new int[numPoints];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(phase + i * Math.PI * hertz
                    / 20.0)
                    * amp);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createFloatSineWave(double phase,
                                                          double hertz,
                                                          int numPoints,
                                                          double amp) {
        float[] dataBits = new float[numPoints];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (float)(Math.sin(phase + i * Math.PI * hertz / 20.0) * amp);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createHighSineWave(double phase,
                                                         double hertz) {
        int[] dataBits = new int[120];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(phase + i * Math.PI * hertz
                    / 20.0) * 1000.0 + 500);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createLowSineWave(double phase,
                                                        double hertz) {
        int[] dataBits = new int[120];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(phase + i * Math.PI * hertz
                    / 20.0) * 1000.0 - 500);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createDelta() {
        Instant now = Instant.now();
        double traceSecs = DEFAULT_TRACE_LENGTH.toNanos()/1000000000.0;
        int[] dataBits = new int[(int)(SPIKE_SAMPLES_PER_SECOND * traceSecs)];
        dataBits[0] = 1;
        return createTestData("kronecker delta at 0",
                              dataBits,
                              now,
                              MockChannelId.makeChanId(now));
    }

    public static LocalSeismogramImpl createSpike() {
        return createSpike(Instant.now());
    }

    public static LocalSeismogramImpl createSpike(ChannelId chanId) {
        return createSpike(Instant.now(), DEFAULT_TRACE_LENGTH, 20, chanId);
    }

    public static LocalSeismogramImpl createSpike(Instant spikeTime) {
        return createSpike(spikeTime, DEFAULT_TRACE_LENGTH);
    }

    public static LocalSeismogramImpl createSpike(Instant spikeTime,
                                                  Duration traceLength) {
        return createSpike(spikeTime,
                           traceLength,
                           20,
                           MockChannelId.makeChanId(spikeTime));
    }

    public static LocalSeismogramImpl createSpike(Instant time,
                                                  Duration traceLength,
                                                  int samplesPerSpike,
                                                  ChannelId id) {
        return createRaggedSpike(time, traceLength, samplesPerSpike, 0, id);
    }

    public static LocalSeismogramImpl createRaggedSpike(Instant time,
                                                        Duration traceLength,
                                                        int samplesPerSpike,
                                                        int missingSamples,
                                                        ChannelId id) {
        return createRaggedSpike(time,
                                 traceLength,
                                 samplesPerSpike,
                                 missingSamples,
                                 id,
                                 SPIKE_SAMPLES_PER_SECOND);
    }

    public static LocalSeismogramImpl createRaggedSpike(Instant time,
                                                        Duration traceLength,
                                                        int samplesPerSpike,
                                                        int missingSamples,
                                                        ChannelId id,
                                                        double samplesPerSecond) {
        double secondShift = missingSamples / samplesPerSecond;
        Duration shiftInt = TimeUtils.durationFromSeconds(secondShift);
        time = time.plus(shiftInt);
        traceLength = traceLength.minus(shiftInt);
        String name = "spike at " + time.toString();
        double traceSecs = TimeUtils.durationToDoubleSeconds(traceLength);
        int[] dataBits = new int[(int)Math.round((samplesPerSecond * traceSecs))];
        for(int i = missingSamples; i < dataBits.length; i+= samplesPerSpike) {
            dataBits[i] = 100;
        }
        return createTestData(name,
                              dataBits,
                              time,
                              id,
                              SamplingImpl.ofSamplesSeconds(dataBits.length, traceSecs));
    }
}
