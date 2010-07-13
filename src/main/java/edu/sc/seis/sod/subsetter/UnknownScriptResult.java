package edu.sc.seis.sod.subsetter;


public class UnknownScriptResult extends Exception {

    public UnknownScriptResult() {
    }

    public UnknownScriptResult(String message) {
        super(message);
    }

    public UnknownScriptResult(Throwable cause) {
        super(cause);
    }

    public UnknownScriptResult(String message, Throwable cause) {
        super(message, cause);
    }
}
