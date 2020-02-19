package edu.sc.seis.sod.hibernate.eventpair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.source.seismogram.SeismogramSourceException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.EventEffectiveTimeOverlap;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;

public class EventStationPair extends CookieEventPair {

    /** for hibernate */
    protected EventStationPair() {}

    public EventStationPair(StatefulEvent event, Station station) {
        this(event, station, Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.INIT));
    }

    public EventStationPair(StatefulEvent event, Station station, Status status) {
        super(event, status);
        setStation(station);
    }

    public void run() {
        SodDB sodDb = SodDB.getSingleton();
        logger.debug("Begin EventStationPair (e="+getEvent().getDbid()+",s="+getStationDbId()+") "+this);
        // make sure origin and station not lazy
        Location l = getEvent().getOrigin().getLocation();
        l = Location.of(getStation());
        // don't bother with station if effective time does not
        // overlap event time
        try {
            EventEffectiveTimeOverlap overlap = new EventEffectiveTimeOverlap(getEvent());
            Map<String, Measurement> cookies = new HashMap<String, Measurement>();
            StringTree accepted = new StringTreeLeaf(this, false);
            try {
                EventStationSubsetter esSub = Start.getWaveformRecipe().getEventStationSubsetter();
                synchronized(esSub) {
                    accepted = esSub.accept(getEvent(), getStation(), getMeasurements());
                }
            } catch(Throwable e) {
                // this might not be right exception???
                if(e instanceof IOException) {
                    update(e, Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.CORBA_FAILURE));
                    updateRetries();
                    failLogger.info("Network or server problem, SOD will continue to retry this item periodically: ("
                            + e.getClass().getName() + ") " + this);
                    logger.debug(this.toString(), e);
                } else {
                    update(e, Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.SYSTEM_FAILURE));
                    failLogger.warn(this.toString(), e);
                }
                SodDB.commit();
                logger.debug("Finish (fail) EStaP: " + this);
                return;
            }
            if(!accepted.isSuccess()) {
                update(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.REJECT));
                SodDB.commit();
                failLogger.info(this + "  " + accepted.toString());
                return;
            }
            SodDB.commit();
            SodDB.getSession().update(this);
            List<AbstractEventPair> chanPairs = new ArrayList<AbstractEventPair>();
            if(Start.getWaveformRecipe() instanceof MotionVectorArm) {
                List<ChannelGroup> chanGroups = Start.getNetworkArm().getSuccessfulChannelGroups(getStation());
                if(chanGroups.size() == 0) {
                    logger.info("No successful channel groups for " + this);
                }
                List<ChannelGroup> overlapList = new ArrayList<ChannelGroup>();
                for(ChannelGroup cg : chanGroups) {
                    if(overlap.overlaps(cg.getChannels()[0])) {
                        overlapList.add(cg);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(cg.getChannels()[0])
                                + "'s channel effective time does not overlap the event time");
                    }
                }
                for(ChannelGroup cg : overlapList) {
                    // use Standing.IN_PROG as we are going to do event-channel
                    // processing here
                    // don't want another thread to pull the ECP from the DB
                    logger.debug("Put EventVectorPair (" + getEventDbId() + ", cg " + cg.getDbid() + " ("
                            + ((Channel)cg.getChannel1()).getDbid() + " "
                            + ((Channel)cg.getChannel2()).getDbid() + " "
                            + ((Channel)cg.getChannel3()).getDbid());
                    EventVectorPair p = new EventVectorPair(getEvent(), cg, Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                                       Standing.IN_PROG), this);
                    chanPairs.add(p);
                    sodDb.put(p);
                }
            } else {
                List<Channel> channels = Start.getNetworkArm().getSuccessfulChannels(getStation());
                if(channels.size() == 0) {
                    logger.info("No successful channels for " + this);
                }
                List<Channel> overlapList = new ArrayList<Channel>();
                for(Channel c : channels) {
                    if(overlap.overlaps(c)) {
                        overlapList.add(c);
                    } else {
                        failLogger.info(ChannelIdUtil.toString(c)
                                + "'s channel effective time does not overlap the event time");
                    }
                    logger.info(ChannelIdUtil.toString(c) + "' passed");
                }
                for(Channel c : overlapList) {
                    // use Standing.IN_PROG as we are going to do event-channel
                    // processing here
                    // don't want another thread to pull the ECP from the DB
                    EventChannelPair p = sodDb.createEventChannelPair(getEvent(), c, this);
                    p.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
                    chanPairs.add(p);
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
            logger.debug("End EventStationPair (e="+getEvent().getDbid()+",s="+getStationDbId()+") "+this);
        } catch(NoPreferredOrigin e) {
            // should never happen
            GlobalExceptionHandler.handle(e);
            SodDB.getSession().update(this);
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SYSTEM_FAILURE));
            SodDB.commit();
            return;
        } finally {
            SodDB.commit();
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
        if (Start.getWaveformRecipe() != null) {
            // might be null if not a real SOD run, ie unit tests or using SOD from another app
            Start.getWaveformRecipe().setStatus(this);
        }
    }

    public boolean equals(Object o) {
        if(!(o instanceof EventStationPair))
            return false;
        EventStationPair ecp = (EventStationPair)o;
        if(ecp.getEventDbId() == getEventDbId() && ecp.getStationDbId() == getStationDbId()) {
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
        return "EventStationPair: (" + getDbid() + " -> "+getEvent().getDbid()+","+getStationDbId()+") " + getEvent() + " " + StationIdUtil.toStringFormatDates(getStation()) + " "
                + getStatus();
    }

    public int getStationDbId() {
        return station.getDbid();
    }

    public Station getStation() {
        return station;
    }

    /** for use by hibernate */
    protected void setStation(Station sta) {
        this.station = sta;
    }

    private Station station;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventStationPair.class);
}
