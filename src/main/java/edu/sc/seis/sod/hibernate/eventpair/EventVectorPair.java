/**
 * EventChannelGroupPair.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.hibernate.eventpair;

import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;

public class EventVectorPair extends AbstractEventChannelPair {

    /** for hibernate */
    protected EventVectorPair() {}

    public EventVectorPair(StatefulEvent event,
                           ChannelGroup channelGroup,
                           EventStationPair esp) {
        this(event, channelGroup, Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.INIT), esp);
    }
    
    public EventVectorPair(StatefulEvent event,
                           ChannelGroup channelGroup,
                           Status status, EventStationPair esp) {
        super(event, status, esp);
        this.channelGroup = channelGroup;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }
    
    protected void setChannelGroup(ChannelGroup cg) {
        channelGroup = cg;
    }

    public void run() {
        try {
            ((MotionVectorArm)Start.getWaveformRecipe()).processMotionVectorArm(this);
            SodDB.commit();
            logger.debug("Finish EVP: "+this);
        } catch(Throwable t) {
            System.err.println(EventChannelPair.BIG_ERROR_MSG);
            t.printStackTrace(System.err);
            GlobalExceptionHandler.handle(EventChannelPair.BIG_ERROR_MSG, t);
            try {
                SodDB.rollback();
                update(t, Status.get(Stage.PROCESSOR, Standing.SYSTEM_FAILURE));
                SodDB.commit();
            } catch(Throwable tt) {
                System.err.println("SOD cannot update status of evp, this indicates a significant problem with the database. SOD is now exiting with shame and dispair");
                Start.cataclysmicFailureOfUnbelievableProportions();
            }
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
    

    ChannelGroup channelGroup;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventVectorPair.class);
}
