package edu.sc.seis.sod.status;

public abstract class StringTree {

    public StringTree(String actorName, boolean status) {
        this.actorName = actorName;
        this.status = status;
    }

    public String toString() {
        return actorName + ":" + (status ? OK : FAIL);
    }

    public boolean isSuccess() {
        return status;
    }

    protected String actorName;

    protected boolean status;

    public static final String OK = "OK";

    public static final String FAIL = "FAIL";
}