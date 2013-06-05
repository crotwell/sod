package edu.sc.seis.sod.source.seismogram;

import edu.sc.seis.sod.source.SodSourceException;


public class SeismogramSourceException extends SodSourceException {

    public SeismogramSourceException() {
    }

    public SeismogramSourceException(String s) {
        super(s);
    }

    public SeismogramSourceException(Throwable t) {
        super(t);
    }

    public SeismogramSourceException(String s, Throwable t) {
        super(s, t);
    }
}
