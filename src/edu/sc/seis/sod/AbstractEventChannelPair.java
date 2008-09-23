package edu.sc.seis.sod;

import edu.sc.seis.sod.hibernate.StatefulEvent;


public abstract class AbstractEventChannelPair extends CookieEventPair {

    protected AbstractEventChannelPair() {
    }

    public AbstractEventChannelPair(StatefulEvent event, EventStationPair esp) {
        super(event);
        setEsp(esp);
    }

    public AbstractEventChannelPair(StatefulEvent event, Status status, EventStationPair esp) {
        super(event, status);
        setEsp(esp);
    }

    /**
     * sets the status on this event channel pair to be status and notifies its
     * parent
     */
    public void update(Status status){
        // this is weird, but calling the setter allows hibernate to autodetect a modified object
        setStatus(status);
        updateRetries();
        getCookies().put("status", status);
        Start.getWaveformArm().setStatus(this);
    }

    protected void setEsp(EventStationPair esp) {
        this.esp = esp;
    }
    public EventStationPair getEsp() {
        return esp;
    }
    protected EventStationPair esp;

    public CookieJar getCookieJar() {
        if (cookieJar == null) {
            cookieJar = new CookieJar(this, getEsp().getCookies(), getCookies());
        }
        return cookieJar;
    }
    private CookieJar cookieJar;
}
