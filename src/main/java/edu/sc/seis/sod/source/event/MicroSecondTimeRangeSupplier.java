package edu.sc.seis.sod.source.event;

import edu.sc.seis.sod.model.common.MicroSecondTimeRange;

public interface MicroSecondTimeRangeSupplier {

    public MicroSecondTimeRange getMSTR();
}
