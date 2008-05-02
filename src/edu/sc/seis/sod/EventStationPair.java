package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EventEffectiveTimeOverlap;

public class EventStationPair extends AbstractEventPair {

    /** for hibernate */
    protected EventStationPair() {}

    public EventStationPair(StatefulEvent event, StationImpl station) {
        super(event);
        setStation(station);
    }

    public EventStationPair(StatefulEvent event,
                            StationImpl station,
                            Status status) {
        super(event, status);
        setStation(station);
    }

    public void run() {
        // don't bother with station if effective time does not
        // overlap event time
        try {
            EventEffectiveTimeOverlap overlap = new EventEffectiveTimeOverlap(getEvent());

            StringTree accepted = new StringTreeLeaf(this, false);
            try {
                synchronized(Start.getWaveformArm().getEventStationSubsetter()) {
                    accepted = Start.getWaveformArm().getEventStationSubsetter().accept(getEvent(),
                                                                                        getStation(),
                                                            getCookies());
                }
            } catch(Throwable e) {
                if(e instanceof org.omg.CORBA.SystemException) {
                    update(e, Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.CORBA_FAILURE));
                    updateRetries();
                    failLogger.info("Network or server problem, SOD will continue to retry this item periodically: ("+e.getClass().getName()+") "+this);
                    logger.debug(this, e);
                } else {
                    update(e, Status.get(Stage.EVENT_STATION_SUBSETTER,
                                             Standing.SYSTEM_FAILURE));
                    failLogger.warn(this, e);
                }
                SodDB.commit();
                logger.debug("Finish (fail) EStaP: "+this);
                return;
            }
            if( ! accepted.isSuccess()) {
                update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                      Standing.REJECT));
                failLogger.info(this + "  " + accepted.toString());
                return;
            }
            if(Start.getWaveformArm().getMotionVectorArm() != null) {
                ChannelGroup[] chanGroups = Start.getNetworkArm().getSuccessfulChannelGroups(getStation());
                List<ChannelGroup> overlapList = new ArrayList<ChannelGroup>();
                for(int i = 0; i < chanGroups.length; i++) {
                    if(overlap.overlaps(chanGroups[i].getChannels()[0])) {
                        overlapList.add(chanGroups[i]);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(chanGroups[i].getChannels()[0].get_id())+"'s channel effective time does not overlap the event time");
                    }
                }
                for(int i = 0; i < chanGroups.length; i++) {
                    EventVectorPair p = new EventVectorPair(getEvent(),
                                                              chanGroups[i],
                                                              Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                         Standing.INIT));
                    sodDb.put(p);
                }
            } else {
                ChannelImpl[] channels = Start.getNetworkArm().getSuccessfulChannels(getStation());
                List<ChannelImpl> overlapList = new ArrayList<ChannelImpl>();
                for(int i = 0; i < channels.length; i++) {
                    if(overlap.overlaps(channels[i])) {
                        overlapList.add(channels[i]);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(channels[i].get_id())+"'s channel effective time does not overlap the event time");
                    }
                }
                for(int i = 0; i < channels.length; i++) {
                    EventChannelPair p = new EventChannelPair(getEvent(),
                                                              channels[i],
                                                              Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                         Standing.INIT));
                    sodDb.put(p);
                }
            }
        } catch(NoPreferredOrigin e) {
            // should never happen
            GlobalExceptionHandler.handle(e);
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                              Standing.SYSTEM_FAILURE));
        } catch(NetworkNotFound e) {
            failLogger.info(StationIdUtil.toString(getStation().get_id())
                    + "'s network could not be found. This is possible, but very unusual.");
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.REJECT));
        }
        update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SUCCESS));
        SodDB.commit();
    }

    /**
     * sets the status on this event network pair to be status and notifies its
     * parent
     */
    public void update(Status status) {
        // this is weird, but calling the setter allows hibernate to autodetect
        // a modified object
        setStatusAsShort(status.getAsShort());
        Start.getWaveformArm().setStatus(this);
    }

    public boolean equals(Object o) {
        if(!(o instanceof EventStationPair))
            return false;
        EventStationPair ecp = (EventStationPair)o;
        if(ecp.getEventDbId() == getEventDbId()
                && ecp.getStationDbId() == getStationDbId()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int code = 47 * getStationDbId();
        code += 23 * getEventDbId();
        return code;
    }

    public String toString() {
        return "EventStationPair: (" + getPairId() + ") " + getEvent() + " "
                + StationIdUtil.toString(getStation()) + " " + getStatus();
    }

    public int getStationDbId() {
        return station.getDbid();
    }

    public StationImpl getStation() {
        return station;
    }

    /** for use by hibernate */
    protected void setStation(StationImpl sta) {
        this.station = sta;
    }

    private StationImpl station;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EventStationPair.class);
}
