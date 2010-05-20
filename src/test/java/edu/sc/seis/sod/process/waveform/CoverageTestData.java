package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

/**
 * @author groves Created on Sep 8, 2004
 */
public class CoverageTestData {

    private CoverageTestData(LocalSeismogramImpl[] seis, Time begin, Time end) {
        this.seis = seis;
        request = new RequestFilter[] {new RequestFilter(null, begin, end)};
    }

    public static CoverageTestData makeTooLateBeginAndTooEarlyEnd() {
        Time rfBegin = spikeBegin.add(ONE_SECOND).getFissuresTime();
        Time rfEnd = spikeEnd.add(ONE_SECOND).getFissuresTime();
        return new CoverageTestData(spikeArray, rfBegin, rfEnd);
    }

    public static CoverageTestData makeTooEarlyEndTime() {
        Time rfEnd = spikeEnd.add(ONE_SECOND).getFissuresTime();
        return new CoverageTestData(spikeArray, timeSpikeBegin, rfEnd);
    }

    public static CoverageTestData makeEqualTimes() {
        return new CoverageTestData(spikeArray, timeSpikeBegin, timeSpikeEnd);
    }

    public static CoverageTestData makeOverage() {
        Time rfBegin = spikeBegin.add(ONE_SECOND).getFissuresTime();
        Time rfEnd = spikeEnd.subtract(ONE_SECOND).getFissuresTime();
        return new CoverageTestData(spikeArray, rfBegin, rfEnd);
    }

    public static CoverageTestData makeNoData() {
        return new CoverageTestData(new LocalSeismogramImpl[0],
                                    timeSpikeBegin,
                                    timeSpikeEnd);
    }

    public static CoverageTestData makeCompleteMiss() {
        Time rfBegin = spikeBegin.add(THREE_HOURS).getFissuresTime();
        Time rfEnd = spikeEnd.add(THREE_HOURS).getFissuresTime();
        return new CoverageTestData(new LocalSeismogramImpl[0], rfBegin, rfEnd);
    }

    public static CoverageTestData makeContigousData() {
        LocalSeismogramImpl firstBit = spikeSeis;
        TimeInterval period = spikeSeis.getSampling().getPeriod();
        MicroSecondDate spikeSeisEnd = spikeSeis.getEndTime();
        MicroSecondDate contigousDataStart = new MicroSecondDate(spikeSeisEnd.add(period));
        LocalSeismogramImpl contigousBit = SimplePlotUtil.createSpike(contigousDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                contigousBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }
    
    public static CoverageTestData makeSlightlySeperatedData(){
        LocalSeismogramImpl firstBit = spikeSeis;
        TimeInterval twoPeriod = (TimeInterval)spikeSeis.getSampling().getPeriod().multiplyBy(2);
        MicroSecondDate spikeSeisEnd = spikeSeis.getEndTime();
        MicroSecondDate uncontigousDataStart = new MicroSecondDate(spikeSeisEnd.add(twoPeriod));
        LocalSeismogramImpl uncontigousBit = SimplePlotUtil.createSpike(uncontigousDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                uncontigousBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }
    
    public static CoverageTestData makeOverlappingData(){
        LocalSeismogramImpl firstBit = spikeSeis;
        MicroSecondDate spikeSeisEnd = spikeSeis.getEndTime();
        MicroSecondDate otherDataStart = new MicroSecondDate(spikeSeisEnd.subtract(ONE_SECOND));
        LocalSeismogramImpl otherBit = SimplePlotUtil.createSpike(otherDataStart);
        LocalSeismogramImpl[] data = new LocalSeismogramImpl[] {firstBit,
                                                                otherBit};
        return new CoverageTestData(data, timeSpikeBegin, timeSpikeEnd);
    }

    private static LocalSeismogramImpl spikeSeis = SimplePlotUtil.createSpike(new MicroSecondDate(20));

    private static LocalSeismogramImpl[] spikeArray = {spikeSeis};

    private static MicroSecondDate spikeBegin = spikeSeis.getBeginTime();

    private static MicroSecondDate spikeEnd = spikeSeis.getEndTime();

    private static Time timeSpikeBegin = spikeBegin.getFissuresTime();

    private static Time timeSpikeEnd = spikeEnd.getFissuresTime();

    public LocalSeismogramImpl[] seis;

    public RequestFilter[] request;

    private static final TimeInterval ONE_SECOND = new TimeInterval(1,
                                                                    UnitImpl.SECOND);

    private static final TimeInterval THREE_HOURS = new TimeInterval(3,
                                                                     UnitImpl.HOUR);
}