package edu.sc.seis.sod;

/**
 * SodException.java
 *
 *
 * Created: Fri Apr 26 10:55:33 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class SodException {
    public SodException (java.lang.Object object, Exception e){
	
	this.object = object;
	this.exception = e;
    }
    
    public java.lang.Object getSource() {
	return this.object;
    }
    
    public Exception getException() {
	return this.exception;
    }

    private java.lang.Object object;
    private Exception exception;
    
}// SodException
