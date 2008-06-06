package edu.sc.seis.sod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.LockMode;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EventEffectiveTimeOverlap;

public class EventStationPair extends CookieEventPair {

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
        // make sure origin and station not lazy
        Location l = getEvent().getOrigin().getLocation();
        l = getStation().getLocation();
        // don't bother with station if effective time does not
        // overlap event time
        try {
            EventEffectiveTimeOverlap overlap = new EventEffectiveTimeOverlap(getEvent());
            Map<String, Serializable> cookies = new HashMap<String, Serializable>();
            StringTree accepted = new StringTreeLeaf(this, false);
            try {
                synchronized(Start.getWaveformArm().getEventStationSubsetter()) {
                    accepted = Start.getWaveformArm().getEventStationSubsetter().accept(getEvent(),
                                                                                        getStation(),
                                                            new CookieJar(this, cookies));
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
            List<AbstractEventPair> chanPairs = new ArrayList<AbstractEventPair>();
            CacheNetworkAccess netAccess = CacheNetworkAccess.create((NetworkAttrImpl)station.getNetworkAttr(), CommonAccess.getNameService());
            if(Start.getWaveformArm().getMotionVectorArm() != null) {
                ChannelGroup[] chanGroups = Start.getNetworkArm().getSuccessfulChannelGroups(netAccess, getStation());
                List<ChannelGroup> overlapList = new ArrayList<ChannelGroup>();
                for(int i = 0; i < chanGroups.length; i++) {
                    if(overlap.overlaps(chanGroups[i].getChannels()[0])) {
                        overlapList.add(chanGroups[i]);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(chanGroups[i].getChannels()[0].get_id())+"'s channel effective time does not overlap the event time");
                    }
                }
                for(int i = 0; i < chanGroups.length; i++) {
                    // use Standing.IN_PROG as we are going to do event-channel processing here
                    // don't want another thread to pull the ECP from the DB
                    EventVectorPair p = new EventVectorPair(getEvent(),
                                                              chanGroups[i],
                                                              Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                         Standing.IN_PROG));
                    chanPairs.add(p);
                    sodDb.put(p);
                }
            } else {
                ChannelImpl[] channels = Start.getNetworkArm().getSuccessfulChannels(netAccess, getStation());
                List<ChannelImpl> overlapList = new ArrayList<ChannelImpl>();
                for(int i = 0; i < channels.length; i++) {
                    if(overlap.overlaps(channels[i])) {
                        overlapList.add(channels[i]);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(channels[i].getId())+"'s channel effective time does not overlap the event time");
                    }
                    logger.info(ChannelIdUtil.toString(channels[i].getId())+"' passed");
                    
                }
                for(int i = 0; i < channels.length; i++) {
                    // use Standing.IN_PROG as we are going to do event-channel processing here
                    // don't want another thread to pull the ECP from the DB
                    EventChannelPair p = new EventChannelPair(getEvent(),
                                                              channels[i],
                                                              Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                         Standing.IN_PROG),
                                                              this);
                    chanPairs.add(p);
                    sodDb.put(p);
                }
            }
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SUCCESS));
            SodDB.commit();
            // run channel pairs now
            for(AbstractEventPair pair : chanPairs) {
                sodDb.getSession().update(pair);
                sodDb.getSession().update(this);
                pair.run();
                SodDB.commit();
            }
        } catch(NoPreferredOrigin e) {
            // should never happen
            GlobalExceptionHandler.handle(e);
            sodDb.getSession().update(this);
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                              Standing.SYSTEM_FAILURE));
            SodDB.commit();
            return;
        } catch(NetworkNotFound e) {
            failLogger.info(StationIdUtil.toString(getStation().get_id())
                    + "'s network could not be found. This is possible, but very unusual.");
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.REJECT));
            sodDb.getSession().update(this);
            SodDB.commit();
            return;
        }
    }

    /**
     * sets the status on this event network pair to be status and notifies its
     * parent
     */
    public void update(Status status) {
        // this is weird, but calling the setter allows hibernate to autodetect
        // a modified object
        setStatus(status);
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
