/**
 * StringTreeBranch.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

public class StringTreeBranch extends StringTree {

    public StringTreeBranch(Object name, boolean boo, StringTree branch) {
        this(name, boo, new StringTree[] { branch });
    }

    public StringTreeBranch(Object name, boolean boo, StringTree[] branches) {
        super(name, boo);
        this.branches = branches;
    }

    public String toString() {
        String s = super.toString()+" (";
        for (int i = 0; i < branches.length; i++) {
            if (branches[i] != null) {
                s += branches[i].toString();
            } else {
                s += "null";
            }
            if (i != branches.length-1) {
                s += ", ";
            }
        }
        return s += ")";
    }

    protected StringTree[] branches;
}


