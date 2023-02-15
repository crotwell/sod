
package edu.sc.seis.sod.model.common;

/**
 * UnsupportedFormat.java signals that a format is not recognized. This
 * could be a an encoding of a date into a string.
 *
 *
 * Created: Wed Sep  1 09:56:18 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class UnsupportedFormat extends RuntimeException {
    
    public UnsupportedFormat() {
	super();
    }
    
    public UnsupportedFormat(String s) {
	super(s);
    }
    
} // UnsupportedFormat
