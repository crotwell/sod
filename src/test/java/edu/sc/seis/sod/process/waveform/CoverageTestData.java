package edu.sc.seis.sod.process.waveform;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.sod.mock.seismogram.MockSeismogram;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.util.time.ClockUtil;

/**
 * @author groves Created on Sep 8, 2004
 */
public class CoverageTestData {

    private CoverageTestData(LocalSeismogramImpl[] seis, Instant begin, Instant end) {
        this.seis = seis;
        request = new RequestFilter[] {new RequestFilter(MockChannel.createChannel(), begin, end)};
    }

    public static CoverageTestData makeTooLateBeginAndTooEarlyEnd() {
        Instant rfBegin = spikeBegin.plus(ONE_SECOND);
        Instant rfEnd = spikeEnd.plus(ONE_SECOND);
        return new CoverageTestData(spikeArray, rfBegin, rfEnd);
    }

    public static CoverageTestData makeTooEarlyEndTime() {
        Instant rfEnd = spikeEnd.plus(ONE_SECOND);
        return new CoverageTestData(spikeArray, timeSpikeBegin, rfEnd);
    }

    public static CoverageTestData makeEqualTimes() {
        return new CoverageTestData(spikeArray, timeSpikeBegin, timeSpikeEnd);
    }

    public static CoverageTestData makeOverage() {
        Instant rfBegin = spikeBegin.plus(ONE_SECOND);
        Instant rfEnd = spikeEnd.minus(ONE_SECOND);
        return new CoverageTestData(spikeArray, rfBegin, rfEnd);
    }

    public static CoverageTestData makeNoData() {
        return new CoverageTestData(new LocalSeismogramImpl[0],
                                    timeSpikeBegin,
                                    timeSpikeEnd);
    }

    public static CoverageTestData makeCompleteMiss() {
        Instant rfBegin = spikeBegin.plus(THREE_HOURS);
        Instant rfEnd = spikeEnd.plus(THREE_HOURS);
        return new CoverageTestData(new LocalSeismogramImpl[0], rfBegin, rfEnd);
    }

    public static CoverageTestData makeContigousData() {
        LocalSeismogramImpl firstBit = spikeSeis;
        Duration period = spikeSeis.getSampling().getPeriod();
        Instant spikeSeisEnd = spikeSeis.getEndTime();
        Instant contigousDataStart = spikeSeisEnd.plus(period);
        LocalSeismogramImpl contigousBit = MockSeismogram.createSpike(contigousDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                contigousBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }
    
    public static CoverageTestData makeSlightlySeperatedData(){
        LocalSeismogramImpl firstBit = spikeSeis;
        Duration twoPeriod = spikeSeis.getSampling().getPeriod().multipliedBy(2);
        Instant spikeSeisEnd = spikeSeis.getEndTime();
        Instant uncontigousDataStart = spikeSeisEnd.plus(twoPeriod);
        LocalSeismogramImpl uncontigousBit = MockSeismogram.createSpike(uncontigousDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                uncontigousBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }
    
    public static CoverageTestData makeOverlappingData(){
        LocalSeismogramImpl firstBit = spikeSeis;
        Instant spikeSeisEnd = spikeSeis.getEndTime();
        Instant otherDataStart = spikeSeisEnd.minus(ONE_SECOND);
        LocalSeismogramImpl otherBit = MockSeismogram.createSpike(otherDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                otherBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }

    private static LocalSeismogramImpl spikeSeis = MockSeismogram.createSpike(Instant.ofEpochMilli(20));

    private static LocalSeismogramImpl[] spikeArray = {spikeSeis};

    private static Instant spikeBegin = spikeSeis.getBeginTime();

    private static Instant spikeEnd = spikeSeis.getEndTime();

    private static Instant timeSpikeBegin = spikeBegin;

    private static Instant timeSpikeEnd = spikeEnd;

    public LocalSeismogramImpl[] seis;

    public RequestFilter[] request;

    private static final Duration ONE_SECOND = ClockUtil.ONE_SECOND;

    private static final Duration THREE_HOURS = Duration.ofHours(3);
}