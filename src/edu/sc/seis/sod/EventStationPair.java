package edu.sc.seis.sod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.DBCacheNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
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
                    accepted = Start.getWaveformArm()
                            .getEventStationSubsetter()
                            .accept(getEvent(),
                                    getStation(),
                                    new CookieJar(this, cookies));
                }
            } catch(Throwable e) {
                if(e instanceof org.omg.CORBA.SystemException) {
                    update(e, Status.get(Stage.EVENT_STATION_SUBSETTER,
                                         Standing.CORBA_FAILURE));
                    updateRetries();
                    failLogger.info("Network or server problem, SOD will continue to retry this item periodically: ("
                            + e.getClass().getName() + ") " + this);
                    logger.debug(this, e);
                } else {
                    update(e, Status.get(Stage.EVENT_STATION_SUBSETTER,
                                         Standing.SYSTEM_FAILURE));
                    failLogger.warn(this, e);
                }
                SodDB.commit();
                logger.debug("Finish (fail) EStaP: " + this);
                return;
            }
            if(!accepted.isSuccess()) {
                update(Status.get(Stage.EVENT_STATION_SUBSETTER,
                                  Standing.REJECT));
                failLogger.info(this + "  " + accepted.toString());
                return;
            }
            // need to evict station so station that comes with channels is ok
            NetworkDB.getSession().evict(station);
            NetworkDB.getSession().evict(station.getNetworkAttr());
            List<AbstractEventPair> chanPairs = new ArrayList<AbstractEventPair>();
            if(Start.getWaveformArm().getMotionVectorArm() != null) {
                List<ChannelGroup> chanGroups = Start.getNetworkArm()
                        .getSuccessfulChannelGroups(getStation());
                if(chanGroups.size() == 0) {
                    logger.info("No successful channel groups for "+this);
                }
                List<ChannelGroup> overlapList = new ArrayList<ChannelGroup>();
                for(ChannelGroup cg : chanGroups) {
                    if(overlap.overlaps(cg.getChannels()[0])) {
                        overlapList.add(cg);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(cg.getChannels()[0].get_id())
                                + "'s channel effective time does not overlap the event time");
                    }
                }
                for(ChannelGroup cg : overlapList) {
                    // use Standing.IN_PROG as we are going to do event-channel
                    // processing here
                    // don't want another thread to pull the ECP from the DB
                    logger.debug("Put EventVectorPair ("
                            + getEventDbId()
                            + ", cg "
                            + cg.getDbid()
                            + " ("
                            + ((ChannelImpl)cg.getChannel1()).getDbid()
                            + " "
                            + ((ChannelImpl)cg.getChannel2()).getDbid()
                            + " "
                            + ((ChannelImpl)cg.getChannel3()).getDbid());
                    EventVectorPair p = new EventVectorPair(getEvent(),
                                                            cg,
                                                            Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                       Standing.IN_PROG),
                                                            this);
                    chanPairs.add(p);
                    sodDb.put(p);
                }
            } else {
                List<ChannelImpl> channels = Start.getNetworkArm()
                        .getSuccessfulChannels(getStation());
                if(channels.size() == 0) {
                    logger.info("No successful channels for "+this);
                }
                List<ChannelImpl> overlapList = new ArrayList<ChannelImpl>();
                for(ChannelImpl c : channels) {
                    if(overlap.overlaps(c)) {
                        overlapList.add(c);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(c.getId())
                                + "'s channel effective time does not overlap the event time");
                    }
                    logger.info(ChannelIdUtil.toString(c.getId()) + "' passed");
                }
                for(ChannelImpl c : channels) {
                    // use Standing.IN_PROG as we are going to do event-channel
                    // processing here
                    // don't want another thread to pull the ECP from the DB
                    EventChannelPair p = new EventChannelPair(getEvent(),
                                                              c,
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
                SodDB.getSession().update(pair);
                SodDB.getSession().update(this);
                pair.run();
                SodDB.commit();
            }
        } catch(NoPreferredOrigin e) {
            // should never happen
            GlobalExceptionHandler.handle(e);
            SodDB.getSession().update(this);
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                              Standing.SYSTEM_FAILURE));
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
        getCookies().put("status", status);
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
        return "EventStationPair: (" + getDbid() + ") " + getEvent() + " "
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
