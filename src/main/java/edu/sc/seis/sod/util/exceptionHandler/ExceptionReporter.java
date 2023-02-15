/**
 * ExceptionReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;

import java.util.List;

public interface ExceptionReporter{
    public void report(String message, Throwable e, List sections) throws Exception;
}

