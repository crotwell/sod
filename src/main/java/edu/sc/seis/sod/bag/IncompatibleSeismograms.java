package edu.sc.seis.sod.bag;

/**
 * IncompatibleSeismograms.java
 *
 *
 * Created: Sat Oct 19 11:38:49 2002
 *
 * @author Philip Crotwell
 */

public class IncompatibleSeismograms extends Exception {
    public IncompatibleSeismograms (String reason){
        super(reason);
    }

    public IncompatibleSeismograms (String reason, Throwable t){
        super(reason, t);
    }
}// IncompatibleSeismograms
