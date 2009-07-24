/**
 * ErrorCountTemplate.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class ErrorCountTemplate extends AllTypeTemplate {
    public String getResult() {
        return "" + GlobalExceptionHandler.getNumHandled();
    }

}

