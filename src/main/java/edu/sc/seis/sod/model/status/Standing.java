/**
 * Standing.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.model.status;

import java.io.Serializable;
import java.lang.reflect.Field;

public class Standing implements Serializable {

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

    public static Standing getForName(String name) throws NoSuchFieldException {
        try {
            Field f = Standing.class.getDeclaredField(name.toUpperCase());
            return (Standing)f.get(null);
        } catch (IllegalAccessException e) {
            throw new NoSuchFieldException("No field with name="+name);
        }
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

