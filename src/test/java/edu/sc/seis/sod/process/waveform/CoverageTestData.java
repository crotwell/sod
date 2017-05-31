package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.sod.mock.seismogram.MockSeismogram;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

/**
 * @author groves Created on Sep 8, 2004
 */
public class CoverageTestData {

    private CoverageTestData(LocalSeismogramImpl[] seis, MicroSecondDate begin, MicroSecondDate end) {
        this.seis = seis;
        request = new RequestFilter[] {new RequestFilter(null, begin, end)};
    }

    public static CoverageTestData makeTooLateBeginAndTooEarlyEnd() {
        MicroSecondDate rfBegin = spikeBegin.add(ONE_SECOND);
        MicroSecondDate rfEnd = spikeEnd.add(ONE_SECOND);
        return new CoverageTestData(spikeArray, rfBegin, rfEnd);
    }

    public static CoverageTestData makeTooEarlyEndTime() {
        MicroSecondDate rfEnd = spikeEnd.add(ONE_SECOND);
        return new CoverageTestData(spikeArray, timeSpikeBegin, rfEnd);
    }

    public static CoverageTestData makeEqualTimes() {
        return new CoverageTestData(spikeArray, timeSpikeBegin, timeSpikeEnd);
    }

    public static CoverageTestData makeOverage() {
        MicroSecondDate rfBegin = spikeBegin.add(ONE_SECOND);
        MicroSecondDate rfEnd = spikeEnd.subtract(ONE_SECOND);
        return new CoverageTestData(spikeArray, rfBegin, rfEnd);
    }

    public static CoverageTestData makeNoData() {
        return new CoverageTestData(new LocalSeismogramImpl[0],
                                    timeSpikeBegin,
                                    timeSpikeEnd);
    }

    public static CoverageTestData makeCompleteMiss() {
        MicroSecondDate rfBegin = spikeBegin.add(THREE_HOURS);
        MicroSecondDate rfEnd = spikeEnd.add(THREE_HOURS);
        return new CoverageTestData(new LocalSeismogramImpl[0], rfBegin, rfEnd);
    }

    public static CoverageTestData makeContigousData() {
        LocalSeismogramImpl firstBit = spikeSeis;
        TimeInterval period = spikeSeis.getSampling().getPeriod();
        MicroSecondDate spikeSeisEnd = spikeSeis.getEndTime();
        MicroSecondDate contigousDataStart = new MicroSecondDate(spikeSeisEnd.add(period));
        LocalSeismogramImpl contigousBit = MockSeismogram.createSpike(contigousDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                contigousBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }
    
    public static CoverageTestData makeSlightlySeperatedData(){
        LocalSeismogramImpl firstBit = spikeSeis;
        TimeInterval twoPeriod = (TimeInterval)spikeSeis.getSampling().getPeriod().multiplyBy(2);
        MicroSecondDate spikeSeisEnd = spikeSeis.getEndTime();
        MicroSecondDate uncontigousDataStart = new MicroSecondDate(spikeSeisEnd.add(twoPeriod));
        LocalSeismogramImpl uncontigousBit = MockSeismogram.createSpike(uncontigousDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                uncontigousBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }
    
    public static CoverageTestData makeOverlappingData(){
        LocalSeismogramImpl firstBit = spikeSeis;
        MicroSecondDate spikeSeisEnd = spikeSeis.getEndTime();
        MicroSecondDate otherDataStart = new MicroSecondDate(spikeSeisEnd.subtract(ONE_SECOND));
        LocalSeismogramImpl otherBit = MockSeismogram.createSpike(otherDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                otherBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }

    private static LocalSeismogramImpl spikeSeis = MockSeismogram.createSpike(new MicroSecondDate(20));

    private static LocalSeismogramImpl[] spikeArray = {spikeSeis};

    private static MicroSecondDate spikeBegin = spikeSeis.getBeginTime();

    private static MicroSecondDate spikeEnd = spikeSeis.getEndTime();

    private static MicroSecondDate timeSpikeBegin = spikeBegin;

    private static MicroSecondDate timeSpikeEnd = spikeEnd;

    public LocalSeismogramImpl[] seis;

    public RequestFilter[] request;

    private static final TimeInterval ONE_SECOND = new TimeInterval(1,
                                                                    UnitImpl.SECOND);

    private static final TimeInterval THREE_HOURS = new TimeInterval(3,
                                                                     UnitImpl.HOUR);
}