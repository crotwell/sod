/**
 * EventChannelPair.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod;

import org.apache.log4j.Logger;

import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;

public class EventChannelPair extends AbstractEventChannelPair {
    
    /** for hibernate */
    protected EventChannelPair() {}
    
    public EventChannelPair(StatefulEvent event, ChannelImpl chan, EventStationPair esp) {
        super(event, esp);
        setChannel(chan);
    }

    public EventChannelPair(StatefulEvent event, ChannelImpl chan, Status status, EventStationPair esp) {
        super(event, status, esp);
        setChannel(chan);
    }

    public void run() {
        try {
            ((LocalSeismogramArm)Start.getWaveformRecipe()).processLocalSeismogramArm(this);
            SodDB.commit();
            logger.debug("Finish ECP: "+this);
        } catch(Throwable t) {
            System.err.println(EventChannelPair.BIG_ERROR_MSG);
            t.printStackTrace(System.err);
            GlobalExceptionHandler.handle(EventChannelPair.BIG_ERROR_MSG, t);
            try {
                SodDB.rollback();
                update(t, Status.get(Stage.PROCESSOR, Standing.SYSTEM_FAILURE));
                SodDB.commit();
            } catch(Throwable tt) {
                System.err.println("SOD cannot update status of ecp, this indicates a significant problem with the database. SOD is now exiting with shame and dispair");
                Start.cataclysmicFailureOfUnbelievableProportions();
            }
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

    private ChannelImpl chan;

    public static final String BIG_ERROR_MSG = "An exception occured that would've croaked a waveform worker thread!  These types of exceptions are certainly possible, but they shouldn't be allowed to percolate this far up the stack.  If you are one of those esteemed few working on SOD, it behooves you to attempt to trudge down the stack trace following this message and make certain that whatever threw this exception is no longer allowed to throw beyond its scope.  If on the other hand, you are a user of SOD it would be most appreciated if you would send an email containing the text immediately following this mesage to sod@seis.sc.edu";

    private static Logger logger = Logger.getLogger(EventChannelPair.class);
}
