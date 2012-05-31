package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;


public class BeginEndTimeRangeSupplier implements MicroSecondTimeRangeSupplier {

    public BeginEndTimeRangeSupplier(MicroSecondDateSupplier begin, MicroSecondDateSupplier end) {
        this.begin = begin;
        this.end = end;
    }
    public MicroSecondTimeRange getMSTR() {
        return new MicroSecondTimeRange(begin.load(), end.load());
    }
    MicroSecondDateSupplier begin;
    MicroSecondDateSupplier end;
}
