package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.subsetter.EventEffectiveTimeOverlap;

public class EventNetworkPair extends AbstractEventPair {

    /** for hibernate */
    protected EventNetworkPair() {}

    public EventNetworkPair(StatefulEvent event, NetworkAttrImpl net) {
        this(event, net, Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.INIT));
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
        List<EventStationPair> alreadyInDb = SodDB.getSingleton().loadESPForNetwork(getEvent(), getNetwork());
        List<EventStationPair> staPairList = new ArrayList<EventStationPair>();
        try {
            EventEffectiveTimeOverlap overlap = new EventEffectiveTimeOverlap(getEvent());
            StationImpl[] stations = Start.getNetworkArm()
                    .getSuccessfulStations(getNetwork());
            logger.debug("Begin EventNetworkPair ("+getEvent().getDbid()+",s "+getNetworkDbId()+") "+this);
            logger.debug(stations.length+" successful stations for "+this);
            for(int i = 0; i < stations.length; i++) {
                logger.debug("Station successful ("+stations[i].getDbid()+") "+StationIdUtil.toString(stations[i]));
            }
            for(int i = 0; i < stations.length; i++) {
                if(!overlap.overlaps(stations[i])) {
                    failLogger.info(StationIdUtil.toString(stations[i].get_id())
                            + "'s station effective time does not overlap the event time.");
                } else {
                    EventStationPair p;
                    try {
                        p = new EventStationPair(getEvent(), 
                                                 NetworkDB.getSingleton().getStation(stations[i].getDbid()));
                        logger.debug("Created ESP "+p);
                    } catch(NotFound e) {
                        throw new RuntimeException("Should never happen but I guess it did!", e);
                    }
                    staPairList.add(p);
                }
            }
            synchronized(WaveformArm.class) {
                update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SUCCESS));
                Iterator<EventStationPair> it = staPairList.iterator();
                while(it.hasNext()) {
                    EventStationPair p = it.next();
                    boolean found = false;
                    for (EventStationPair dbEsp : alreadyInDb) {
                        if (dbEsp.getStationDbId() == p.getStationDbId()) {
                            found = true;
                            p = dbEsp;
                            break;
                        }
                    }
                    if (found) {
                        if (! (p.getStatus().getStanding().equals(Standing.REJECT) 
                                || p.getStatus().getStanding().equals(Standing.SUCCESS))) {
                            p.update(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                              Standing.INIT));
                        SodDB.getSession().update(p);
                        } else {
                            it.remove();
                        }
                    } else {
                        SodDB.getSession().save(p);
                    }
                }
                SodDB.commit();
            }
            SodDB.getSingleton().offerEventStationPair(staPairList);
        } catch(NoPreferredOrigin e) {
            // should never happen
            GlobalExceptionHandler.handle(e);
            update(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                              Standing.SYSTEM_FAILURE));
            failLogger.warn(this.toString(), e);
        } finally {
            // make sure session is closed in case of exception
            SodDB.rollback();
        }
        logger.debug("End EventNetworkPair ("+getEvent().getDbid()+",s "+getNetworkDbId()+") "+this);
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
