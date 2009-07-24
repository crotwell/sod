package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;

public class StringTreeLeaf extends StringTree {

    public StringTreeLeaf(Object actor, boolean status, String reason) {
        this(actor, status, reason, null);
    }

    public StringTreeLeaf(Object actor,
                          boolean status,
                          String reason,
                          Throwable t) {
        this(ExceptionReporterUtils.getClassName(actor), status, reason, t);
    }

    public StringTreeLeaf(String actorName,
                          boolean status,
                          String reason,
                          Throwable t) {
        super(actorName, status);
        this.reason = reason;
        this.t = t;
    }

    public StringTreeLeaf(Object actor, boolean boo) {
        this(actor, boo, "");
    }

    public StringTreeLeaf(String name, boolean boo) {
        this(name, boo, "");
    }

    public String toString(int indentationLevel) {
        return super.toString(indentationLevel)
                + (reason != null ? ":" + reason : "")
                + (t != null ? ":" + t : "");
    }

    protected String reason;

    protected Throwable t;
}