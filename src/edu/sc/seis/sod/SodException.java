package edu.sc.seis.sod;

import org.apache.log4j.*;

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
    public SodException (java.lang.Object object, Throwable e){

    this.object = object;
    this.exception = e;
    }

    public java.lang.Object getSource() {
    return this.object;
    }

    public Throwable getThrowable() {
    return this.exception;
    }

    private java.lang.Object object;
    private Throwable exception;

    static Category logger =
        Category.getInstance(SodException.class.getName());

}// SodException
