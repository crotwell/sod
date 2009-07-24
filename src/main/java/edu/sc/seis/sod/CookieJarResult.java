/**
 * CookieJarResult.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

public class CookieJarResult {

    protected CookieJarResult(int pairId, String name) {
        this.pairId = pairId;
        this.name = name;
    }

    public CookieJarResult(int pairId, String name, String value) {
        this(pairId, name);
        this.valueStr = value;
    }

    public CookieJarResult(int pairId, String name, double value) {
        this(pairId, name);
        this.valueDbl = value;
    }

    public CookieJarResult(int pairId, String name, Object value) {
        this(pairId, name);
        this.valueObj = value;
    }

    public int getPairId() { return pairId; }

    public String getName() { return name; }

    public String getValueString() { return valueStr; }

    public double getValueDouble() { return valueDbl; }

    public Object getValueObject() { return valueObj; }

    int pairId;
    String name;
    String valueStr;
    double valueDbl;
    Object valueObj;

}

