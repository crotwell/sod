package edu.sc.seis.sod.source.event;

import edu.sc.seis.sod.model.common.TimeRange;

public interface MicroSecondTimeRangeSupplier {

    public TimeRange getMSTR();
}
