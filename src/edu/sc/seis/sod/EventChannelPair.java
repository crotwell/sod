/**
 * EventChannelPair.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import org.apache.log4j.Logger;

public class EventChannelPair{
    public EventChannelPair(EventDbObject event, ChannelDbObject chan,
                            WaveformArm owner, int pairId){
        this.event = event;
        this.chan = chan;
        this.owner = owner;
        this.pairId = pairId;
    }

    public int getPairId(){ return pairId; }

    public void update(Throwable e, Status status) {
        GlobalExceptionHandler.handle(toString(), e);
        update(status);
    }

    /**
     * sets the status on this event channel pair to be status and notifies its
     * parent
     */
    public void update(Status status){
        setStatus(status);
        owner.setStatus(this);
    }

    /**
     * sets the status on this event channel pair to be status
     */
    public void setStatus(Status status){
        this.status = status;
    }

    public boolean equals(Object o){
        if(!(o instanceof EventChannelPair)) return false;
        EventChannelPair ecp = (EventChannelPair)o;
        if(ecp.getEventDbId() == getEventDbId() &&
           ecp.getChannelDbId() == chan.getDbId()){
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

    public int getChannelDbId(){ return chan.getDbId(); }

    public int getEventDbId() { return event.getDbId(); }

    public Status getStatus(){ return status; }

    public Channel getChannel() { return chan.getChannel(); }

    public CacheEvent getEvent(){ return event.getEvent(); }

    private Status status;
    private EventDbObject event;
    private ChannelDbObject chan;
    private WaveformArm owner;
    private int pairId;
    private static Logger logger = Logger.getLogger(EventChannelPair.class);
}
