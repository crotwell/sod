package edu.sc.seis.sod.status;

public abstract class StringTree {

    public StringTree(String actorName, boolean status) {
        this.actorName = actorName;
        this.status = status;
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int indentationLevel) {
        return getIndent(indentationLevel) + actorName + ":"
                + (status ? OK : FAIL);
    }

    public boolean isSuccess() {
        return status;
    }

    protected String getIndent(int indentationLevel) {
        StringBuffer buff = new StringBuffer(indentationLevel * 2 + 1);
        for(int i = 0; i < (indentationLevel - 1)*2; i++){
            buff.append(' ');
        }
        if(indentationLevel > 0){
        buff.append("|--");
        }
        return buff.toString();
    }

    protected String actorName;

    protected boolean status;

    public static final String OK = "OK";

    public static final String FAIL = "FAIL";
}