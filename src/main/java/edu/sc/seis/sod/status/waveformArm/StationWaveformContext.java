/**
 * StationWaveformContext.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.hibernate.SodDB;

public class StationWaveformContext  extends AbstractContext {

    public StationWaveformContext(StationImpl station) throws SQLException {
        super();
        this.stationId = station;
        jdbcECS = SodDB.getSingleton();
    }
    public StationWaveformContext(Context context, StationImpl station) throws SQLException {
        super(context);
        this.stationId = station;
        jdbcECS = SodDB.getSingleton();
    }


    public Object internalGet(String key) {
        if (key.equals(ALL_EVENTS)) {
            List ecps = jdbcECS.getEventsForStation(stationId);
            // use a set to remove duplicate events
            HashSet out = new HashSet();
            out.addAll(ecps);
            return out;
        } else if (key.equals(SUCCESS_EVENTS_KEY)) {
            List ecps = jdbcECS.getSuccessfulEventsForStation(stationId);
            // use a set to remove duplicate events
            HashSet out = new HashSet();
            out.addAll(ecps);
            return out;
        } else {
            throw new RuntimeException("can't get for key="+key);
        }
    }

    public boolean internalContainsKey(Object key) {
        if (key.equals(ALL_EVENTS)
            || key.equals(SUCCESS_EVENTS_KEY)
            || key.equals(SUCCESS_ECPS_KEY)
            || key.equals(SUCCESS_ECGROUP_KEY)) {
            return true;
        } else {
            throw new RuntimeException("can't get for key="+key);
        }
    }

    public Object[] internalGetKeys() {
        return new String[] {ALL_EVENTS};
    }

    public Object internalRemove(Object key) {
        throw new RuntimeException("Read only context, operation remove not permitted: key="+key);
    }

    public Object internalPut(String key, Object p2) {
        throw new RuntimeException("Read only context, operation put not permitted: key="+key);
    }

    protected StationImpl stationId;
    
    SodDB jdbcECS;

    public static final String ALL_EVENTS = "station_events";

    public static final String SUCCESS_EVENTS_KEY = "successful_station_events";

    public static final String SUCCESS_ECPS_KEY = "successful_event_channels";

    public static final String SUCCESS_ECGROUP_KEY = "successful_event_channel_groups";
}

