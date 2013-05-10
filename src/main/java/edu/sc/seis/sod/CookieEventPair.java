package edu.sc.seis.sod;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.sc.seis.sod.hibernate.StatefulEvent;

public abstract class CookieEventPair extends AbstractEventPair {

    public CookieEventPair() {
        super();
    }

    public CookieEventPair(StatefulEvent event, Status status) {
        super(event, status);
    }

    public CookieEventPair(StatefulEvent event) {
        super(event);
    }

    protected void setStatus(Status status) {
        super.setStatus(status);
    }
    // hibernate
    protected void setCookies(Map<String, Serializable> cookies) {
        this.cookies = cookies;
    }

    public Map<String, Serializable> getCookies() {
        return cookies;
    }

    private Map<String, Serializable> cookies = new HashMap<String, Serializable>();
}
