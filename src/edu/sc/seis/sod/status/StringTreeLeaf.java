/**
 * StringTreeLeaf.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

public class StringTreeLeaf extends StringTree {

    public StringTreeLeaf(Object name, boolean boo, String string) {
        this(name, boo);
        this.string = string;
    }


    public StringTreeLeaf(Object name, boolean boo, String string, Throwable t) {
        this(name, boo, string);
        this.t = t;
    }

    public StringTreeLeaf(Object name, boolean boo) {
        this(name, boo, "");
    }

    public String toString() {
        return super.toString()+" :"+string+"  "+t;
    }

    protected String string;

    protected Throwable t = null;

}

