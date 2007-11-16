/**
 * EventChannelPair.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class EventChannelPair{
    
    /** for hibernate */
    protected EventChannelPair() {}
    
    public EventChannelPair(CacheEvent event, ChannelImpl chan, int pairId) throws SQLException {
        this(event, chan, pairId, null);
    }

    public EventChannelPair(CacheEvent event, ChannelImpl chan, int pairId, Status status) throws SQLException {
        this.event = event;
        this.chan = chan;
        this.pairId = pairId;
        this.status = status;
    }

    public int getPairId(){ return pairId; }
    
    // for hibernate
    protected void setPairId(int pairId) {
        this.pairId = pairId;
    }

    public void update(Throwable e, Status status) {
        GlobalExceptionHandler.handle(toString(), e);
        update(status);
    }

    /**
     * sets the status on this event channel pair to be status and notifies its
     * parent
     */
    public void update(Status status){
        this.status = status;
        getCookieJar().updateStatus();
        Start.getWaveformArm().setStatus(this);
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
        return "EventChannelPair: " + getEvent() + " " +
            ChannelIdUtil.toString(getChannel().get_id()) + " " + getStatus();
    }

    public int getChannelDbId(){ return chan.getDbid(); }

    public int getEventDbId() { return event.getDbid(); }

    public Status getStatus(){ return status; }

    public ChannelImpl getChannel() { return chan; }

    public CacheEvent getEvent(){ return event; }

    public CookieJar getCookieJar() {
        if (cookieJar == null) {
            try {
                this.cookieJar = new CookieJar(this);
            } catch(SQLException e) {
                throw new RuntimeException("Unable to create CookieJar", e);
            }
        }
        return cookieJar; 
    }
    
    /** for use by hibernate */
    protected short getStatusAsShort() {
        return status.getAsShort();
    }

    /** for use by hibernate */
    protected void setEvent(CacheEvent e) {
        this.event = e;
    }
    /** for use by hibernate */
    protected void setChannel(ChannelImpl chan) {
        this.chan = chan;
    }

    /** for use by hibernate */
    protected void setStatusAsShort(short status) {
        this.status = Status.getFromShort(status);
    }
    
    private Status status;
    private CacheEvent event;
    private ChannelImpl chan;
    private int pairId;
    private CookieJar cookieJar;
    private static Logger logger = Logger.getLogger(EventChannelPair.class);
}
