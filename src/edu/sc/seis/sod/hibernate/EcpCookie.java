package edu.sc.seis.sod.hibernate;

import java.io.Serializable;

import edu.sc.seis.sod.EventChannelPair;

public class EcpCookie {

    /** for hibernate */
    protected EcpCookie() {}

    public EcpCookie(EventChannelPair ecp, String name, double value) {
        this(ecp, name, new Double(value));
    }

    public EcpCookie(EventChannelPair ecp, String name, Serializable value) {
        super();
        this.id = new EcpCookieId(ecp.getPairId(), name);
        this.value = value;
        this.ecp = ecp;
    }

    public String getName() {
        return id.getName();
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    public String getValueString() {
        if(value instanceof String) {
            return (String)getValue();
        } else {
            return null;
        }
    }

    public double getValueDouble() {
        if(value instanceof Double) {
            return ((Double)getValue()).doubleValue();
        } else {
            throw new RuntimeException("Not a double");
        }
    }

    public Object getValueObject() {
        return getValue();
    }

    public EventChannelPair getEcp() {
        return ecp;
    }

    protected void setEcp(EventChannelPair ecp) {
        this.ecp = ecp;
    }

    protected EcpCookieId getId() {
        return id;
    }

    protected void setId(EcpCookieId id) {
        this.id = id;
    }

    protected EventChannelPair ecp;

    protected EcpCookieId id;

    protected Serializable value;
}
