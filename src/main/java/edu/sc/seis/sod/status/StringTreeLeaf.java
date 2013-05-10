package edu.sc.seis.sod.status;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        String throwableAsString = "";
        if (getThrowable() != null) {
            StringWriter s = new StringWriter();
            PrintWriter pw = new PrintWriter(s);
            getThrowable().printStackTrace(pw);
            pw.flush();
            throwableAsString = s.toString();
            pw.close();
        }
        return super.toString(indentationLevel)
                + (reason != null ? ":" + reason : "")
                + (t != null ? ":" + t +"\n"+throwableAsString : "");
    }

    
    public String getReason() {
        return reason;
    }

    
    public Throwable getThrowable() {
        return t;
    }

    protected String reason;

    protected Throwable t;
}