package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;

public class StringTreeBranch extends StringTree {

    public StringTreeBranch(Object actor, boolean status, StringTree branch) {
        this(actor, status, new StringTree[] {branch});
    }

    public StringTreeBranch(Object actor, boolean status, StringTree[] branches) {
        this(ExceptionReporterUtils.getClassName(actor), status, branches);
    }

    public StringTreeBranch(String actorName,
                            boolean status,
                            StringTree[] branches) {
        super(actorName, status);
        this.branches = branches;
    }

    public String toString(int indentationLevel) {
        String s = "";
        if(indentationLevel == 0) {
            s += '\n';
        }
        s += super.toString(indentationLevel);
        for(int i = 0; i < branches.length; i++) {
            if(branches[i] instanceof ShortCircuit){
                break;
            }
            s += "\n" + branches[i].toString(indentationLevel + 1);
        }
        return s;
    }

    protected StringTree[] branches;
}
