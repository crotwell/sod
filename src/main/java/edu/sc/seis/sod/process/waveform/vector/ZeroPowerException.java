package edu.sc.seis.sod.process.waveform.vector;


public class ZeroPowerException extends Exception {

    public ZeroPowerException() {
    }

    public ZeroPowerException(String message) {
        super(message);
    }

    public ZeroPowerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZeroPowerException(Throwable cause) {
        super(cause);
    }
}
