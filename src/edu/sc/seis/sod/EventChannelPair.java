/**
 * EventChannelPair.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod;

import org.apache.log4j.Logger;

import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;

public class EventChannelPair extends CookieEventPair {
    
    /** for hibernate */
    protected EventChannelPair() {}
    
    public EventChannelPair(StatefulEvent event, ChannelImpl chan, EventStationPair esp) {
        super(event);
        setChannel(chan);
        setEsp(esp);
    }

    public EventChannelPair(StatefulEvent event, ChannelImpl chan, Status status, EventStationPair esp) {
        super(event, status);
        setChannel(chan);
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

    public void run() {
        try {
            Start.getWaveformArm().getLocalSeismogramArm().processLocalSeismogramArm(this);
            SodDB.commit();
            logger.debug("Finish ECP: "+this);
        } catch(Throwable t) {
            System.err.println(WaveformArm.BIG_ERROR_MSG);
            t.printStackTrace(System.err);
            GlobalExceptionHandler.handle(WaveformArm.BIG_ERROR_MSG, t);
            SodDB.rollback();
        }
    }

    public boolean equals(Object o){
        if(!(o instanceof EventChannelPair)) return false;
        EventChannelPair ecp = (EventChannelPair)o;
        if(ecp.getEventDbId() == getEventDbId() &&
           ecp.getChannelDbId() == chan.getDbid()){
            return true;
        }
        return false;
    }

    public int hashCode(){
        int code = 47 * getChannelDbId();
        code += 23 * getEventDbId();
        return code;
    }

    public String toString(){
        return "EventChannelPair: ("+getDbid()+") " + getEvent() + " " +
            ChannelIdUtil.toString(getChannel().getId()) + " " + getStatus();
    }

    public int getChannelDbId(){ return chan.getDbid(); }

    public ChannelImpl getChannel() { return chan; }
    
    /** for use by hibernate */
    protected void setChannel(ChannelImpl chan) {
        this.chan = chan;
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
    
    private ChannelImpl chan;
    private CookieJar cookieJar;
    private static Logger logger = Logger.getLogger(EventChannelPair.class);
}
