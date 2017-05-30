/**
 * UnsupportedFileTypeException.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.hibernate;

public class UnsupportedFileTypeException extends Exception
{
    public UnsupportedFileTypeException() {
        super();
    }

    public UnsupportedFileTypeException(String msg) {
        super(msg);
    }

    public UnsupportedFileTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

