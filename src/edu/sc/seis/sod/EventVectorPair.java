/**
 * EventChannelGroupPair.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod;

import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;

public class EventVectorPair extends AbstractEventChannelPair {

    /** for hibernate */
    protected EventVectorPair() {}

    public EventVectorPair(StatefulEvent event,
                           ChannelGroup chans,
                           Status status, EventStationPair esp) {
        super(event, status, esp);
        channels = chans;
    }

    public ChannelGroup getChannelGroup() {
        return channels;
    }
    
    protected void setChannelGroup(ChannelGroup cg) {
        channels = cg;
    }

    public void run() {
        try {
            Start.getWaveformArm().getMotionVectorArm().processMotionVectorArm(this);
            SodDB.commit();
            logger.debug("Finish ECP: "+this);
        } catch(Throwable t) {
            System.err.println(WaveformArm.BIG_ERROR_MSG);
            t.printStackTrace(System.err);
            GlobalExceptionHandler.handle(WaveformArm.BIG_ERROR_MSG, t);
            SodDB.rollback();
        }
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof EventVectorPair))
            return false;
        EventVectorPair ecp = (EventVectorPair)o;
        if(ecp.getEventDbId() == getEventDbId()
                && ecp.getChannelGroup().getChannels()[0].getDbid() == getChannelGroup().getChannels()[0].getDbid()
                && ecp.getChannelGroup().getChannels()[1].getDbid() == getChannelGroup().getChannels()[1].getDbid()
                && ecp.getChannelGroup().getChannels()[2].getDbid() == getChannelGroup().getChannels()[2].getDbid()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int code = 47 * getChannelGroup().getChannels()[0].getDbid();
        code += 17 * getChannelGroup().getChannels()[1].getDbid();
        code += 19 * getChannelGroup().getChannels()[2].getDbid();
        code += 23 * getEventDbId();
        return code;
    }

    public String toString() {
        String s = "ECGroup: " + getEvent() + " ";
        s += ChannelIdUtil.toString(getChannelGroup().getChannels()[0].get_id()) + " , ";
        s += ChannelIdUtil.toString(getChannelGroup().getChannels()[1].get_id()) + " , ";
        s += ChannelIdUtil.toString(getChannelGroup().getChannels()[2].get_id()) + " , ";
        s += " " + getStatus();
        return s;
    }
    

    ChannelGroup channels;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EventVectorPair.class);
}