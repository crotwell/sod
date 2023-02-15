package edu.sc.seis.sod.model.common;

import java.time.Instant;

import edu.sc.seis.seisFile.TimeUtils;

/**
 * @author groves Created on Oct 28, 2004
 */
public class TimeFormatter {


    @Deprecated
    public static synchronized String format(Instant t) {
        return TimeUtils.toISOString(t);
    }

}