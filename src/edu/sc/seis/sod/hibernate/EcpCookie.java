package edu.sc.seis.sod.hibernate;

import java.io.Serializable;

import edu.sc.seis.sod.EventChannelPair;

public class EcpCookie {

    /** for hibernate */
    protected EcpCookie() {}
    
    public EcpCookie(EventChannelPair ecp, String name, Serializable value) {
        super();
        this.ecp = ecp;
        this.name = name;
        this.value = value;
    }

    public EventChannelPair getEcp() {
        return ecp;
    }

    protected void setEcp(EventChannelPair ecp) {
        this.ecp = ecp;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    protected Long id;

    protected EventChannelPair ecp;

    protected String name;

    protected Serializable value;
}
