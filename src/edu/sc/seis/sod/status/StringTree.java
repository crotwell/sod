/**
 * StringTree.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

public abstract class StringTree {

    public StringTree(Object name, boolean boo) {
        this.name = name;
        this.boo = boo;
    }

    public String toString() {
        return name+":"+( boo ? OK : FAIL );
    }

    protected Object name;

    protected boolean boo;

    public static final String OK = "OK";
    public static final String FAIL = "FAIL";
}

