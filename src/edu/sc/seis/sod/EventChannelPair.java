/**
 * EventChannelPair.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import org.apache.log4j.Logger;

public class EventChannelPair{
    public EventChannelPair(NetworkDbObject net, EventDbObject event,
                            ChannelDbObject chan, WaveFormArm owner, int pairId){
        this.event = event;
        this.chan = chan;
        this.owner = owner;
        this.net = net;
        this.pairId = pairId;
    }
    
    public int getPairId(){ return pairId; }

    public void update(Exception e, String info, EventChannelCondition status) {
        if (e instanceof FissuresException) {
            FissuresException fe = (FissuresException)e;
            info = info+" FissuresException: code="+fe.the_error.error_code+" "+fe.the_error.error_description;
        }
        CommonAccess.handleException(info, e);
        update(info, status);
    }

    public void update(String info, EventChannelCondition status){
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

    public EventChannelCondition getStatus(){ return status; }

    public String getInfo(){ return info; }

    public Channel getChannel() { return chan.getChannel(); }

    public CacheEvent getEvent(){ return event.getGetEvent(); }

    public NetworkAccess getNet(){ return net.getNetworkAccess(); }

    private String info;

    private EventChannelCondition status;

    private EventDbObject event;

    private ChannelDbObject chan;

    private WaveFormArm owner;

    private NetworkDbObject net;
    
    private int pairId;

    private static Logger logger = Logger.getLogger(EventChannelPair.class);
}
