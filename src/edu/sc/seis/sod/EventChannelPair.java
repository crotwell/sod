/**
 * EventChannelPair.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.Status;
import org.apache.log4j.Logger;

public class EventChannelPair{
    public EventChannelPair(NetworkDbObject net, EventDbObject event,
                            ChannelDbObject chan, WaveFormArm owner){
        this.event = event;
        this.chan = chan;
        this.owner = owner;
        this.net = net;
    }
    
    public void update(Exception e, String info, Status status) throws InvalidDatabaseStateException {
        logger.error(info, e);
        update(info, status);
    }
    
    public void update(String info, Status status)throws InvalidDatabaseStateException{
        this.info = info;
        this.status = status;
        if(owner != null)owner.setStatus(this);
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
    
    public int getChannelDbId(){ return chan.getDbId(); }
    
    public int getEventDbId() { return event.getDbId(); }
    
    public Status getStatus(){ return status; }
    
    public String getInfo(){ return info; }
    
    public Channel getChannel() { return chan.getChannel(); }
    
    public EventAccessOperations getEvent(){ return event.getEventAccess(); }
    
    public NetworkAccess getNet(){ return net.getNetworkAccess(); }
    
    private String info;
    
    private Status status;
    
    private EventDbObject event;
    
    private ChannelDbObject chan;
    
    private WaveFormArm owner;
    
    private NetworkDbObject net;
    
    private static Logger logger = Logger.getLogger(EventChannelPair.class);
}
