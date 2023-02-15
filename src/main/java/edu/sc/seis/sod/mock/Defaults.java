package edu.sc.seis.sod.mock;

import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;

public class Defaults{
    

    public static final Instant EPOCH =  Instant.ofEpochMilli(0);
    
    public static final Instant WALL_FALL = ZonedDateTime.of(1990, Month.JUNE.getValue(), 13, 12, 0, 0, 0, TimeUtils.TZ_UTC).toInstant();
    
    public static final Instant EPOCH_ZDT =  Instant.ofEpochSecond(0);
    
    public static final Instant WALL_FALL_ZDT = Instant.parse("1990-06-13T12:00:00.000Z");
    
    public static final QuantityImpl ZERO_K = new QuantityImpl(0, UnitImpl.KILOMETER);
    
    public static final QuantityImpl TEN_K = new QuantityImpl(10, UnitImpl.KILOMETER);
}
