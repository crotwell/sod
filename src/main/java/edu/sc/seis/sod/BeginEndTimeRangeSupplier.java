package edu.sc.seis.sod;

import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;


public class BeginEndTimeRangeSupplier implements MicroSecondTimeRangeSupplier {

    public BeginEndTimeRangeSupplier(MicroSecondDateSupplier begin, MicroSecondDateSupplier end) {
        this.begin = begin;
        this.end = end;
    }
    public TimeRange getMSTR() {
        return new TimeRange(begin.load(), end.load());
    }
    MicroSecondDateSupplier begin;
    MicroSecondDateSupplier end;
}
