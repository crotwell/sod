/**
 * Standing.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

public class Standing {

    private Standing(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public int getVal() {
        return val;
    }

    public static Standing getFromInt(int val) {
        return ALL[val];
    }

    public static Standing getForName(String name) {
        for (int i = 0; i < ALL.length; i++) {
            if (ALL[i].toString().equals(name)) {return ALL[i];}
        }
        throw new IllegalArgumentException("No Standing for name="+name);
    }


    public static final Standing INIT           = new Standing(0, "");
    public static final Standing IN_PROG        = new Standing(1, "in progress");
    public static final Standing REJECT         = new Standing(2, "rejected");
    public static final Standing RETRY          = new Standing(3, "scheduled for retry");
    public static final Standing CORBA_FAILURE  = new Standing(4, "had a corba failure");
    public static final Standing SYSTEM_FAILURE = new Standing(5, "had a system failure");
    public static final Standing SUCCESS        = new Standing(6, "success");

    public static final Standing[] ALL = {
            INIT,
            IN_PROG,
            REJECT,
            RETRY,
            CORBA_FAILURE,
            SYSTEM_FAILURE,
            SUCCESS
    };

    private int val;

    private String name;
}

