package edu.sc.seis.sod.source;


public class SodSourceException extends Exception {

    public SodSourceException() {
    }

    public SodSourceException(String s) {
        super(s);
    }

    public SodSourceException(Throwable t) {
        super(t);
    }

    public SodSourceException(String s, Throwable t) {
        super(s, t);
    }
}
