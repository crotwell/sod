package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.subsetter.EventEffectiveTimeOverlap;

public class EventNetworkPair extends AbstractEventPair {

    /** for hibernate */
    protected EventNetworkPair() {}

    public EventNetworkPair(StatefulEvent event, NetworkAttrImpl net) {
        super(event);
        setNetwork(net);
    }

    public EventNetworkPair(StatefulEvent event,
                            NetworkAttrImpl net,
                            Status status) {
        super(event, status);
        setNetwork(net);
    }

    public void run() {
        // don't bother with station if effective time does not
        // overlap event time
        try {
            EventEffectiveTimeOverlap overlap = new EventEffectiveTimeOverlap(getEvent());
            StationImpl[] stations = Start.getNetworkArm()
                    .getSuccessfulStations(getNetwork());
            logger.debug("Begin EventNetworkPair ("+getEvent().getDbid()+",s "+getNetworkDbId()+") "+this);
            for(int i = 0; i < stations.length; i++) {
                logger.debug("Station successful ("+stations[i].getDbid()+") "+StationIdUtil.toString(stations[i]));
            }
            for(int i = 0; i < stations.length; i++) {
                if(!overlap.overlaps(stations[i])) {
                    failLogger.info(StationIdUtil.toString(stations[i].get_id())
                            + "'s station effective time does not overlap the event time.");
                } else {
                    EventStationPair p = new EventStationPair(getEvent(),
                                                              stations[i],
                                                              Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                         Standing.INIT));
                    SodDB.getSingleton().put(p);
                }
            }
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SUCCESS));
        } catch(NoPreferredOrigin e) {
            // should never happen
            GlobalExceptionHandler.handle(e);
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                              Standing.SYSTEM_FAILURE));
            failLogger.warn(this.toString(), e);
        }
        logger.debug("End EventNetworkPair ("+getEvent().getDbid()+",s "+getNetworkDbId()+") "+this);
        SodDB.commit();
    }

    /**
     * sets the status on this event network pair to be status and notifies its
     * parent
     */
    public void update(Status status) {
        // this is weird, but calling the setter allows hibernate to autodetect
        // a modified object
        setStatus(status);
        Start.getWaveformRecipe().setStatus(this);
    }

    public boolean equals(Object o) {
        if(!(o instanceof EventNetworkPair))
            return false;
        EventNetworkPair ecp = (EventNetworkPair)o;
        if(ecp.getEventDbId() == getEventDbId()
                && ecp.getNetworkDbId() == getNetworkDbId()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int code = 47 * getNetworkDbId();
        code += 23 * getEventDbId();
        return code;
    }

    public String toString() {
        return "EventNetworkPair: (" + getDbid() + ") " + getEvent() + " "
                + NetworkIdUtil.toStringNoDates(getNetwork()) + " " + getStatus();
    }

    public int getNetworkDbId() {
        return getNetwork().getDbid();
    }

    public NetworkAttrImpl getNetwork() {
        return networkAttr;
    }
    
    /** for use by hibernate */
    protected void setNetwork(NetworkAttrImpl attr) {
        this.networkAttr = attr;
    }
    
    private NetworkAttrImpl networkAttr;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventNetworkPair.class);
}
