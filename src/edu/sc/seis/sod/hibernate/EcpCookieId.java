package edu.sc.seis.sod.hibernate;

import java.io.Serializable;

public class EcpCookieId implements Serializable {

    protected Integer ecp_id;

    protected String name;

    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 29 * result + ecp_id.hashCode();
        return result;
    }

    public boolean equals(Object other) {
        if(other == null)
            return false;
        if(!(other instanceof EcpCookieId))
            return false;
        EcpCookieId that = (EcpCookieId)other;
        return this.name.equals(that.name) && this.ecp_id.equals(that.ecp_id);
    }

    protected void setName(String name) {
        this.name = name;
    }

    public EcpCookieId(int ecp, String name) {
        super();
        this.ecp_id = new Integer(ecp);
        this.name = name;
    }

    protected EcpCookieId() {}

    protected Integer getEcp_id() {
        return ecp_id;
    }

    protected void setEcp_id(Integer ecp_id) {
        this.ecp_id = ecp_id;
    }

    public String getName() {
        return name;
    }
}
