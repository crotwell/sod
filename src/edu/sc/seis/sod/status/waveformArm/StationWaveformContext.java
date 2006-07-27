/**
 * StationWaveformContext.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Start;
import java.sql.SQLException;
import java.util.HashSet;
import org.apache.velocity.context.Context;

public class StationWaveformContext  extends WaveformArmContext {

    public StationWaveformContext(int stationId) throws SQLException {
        super();
        this.stationId = stationId;
    }
    public StationWaveformContext(Context context, int stationId) throws SQLException {
        super(context);
        this.stationId = stationId;
    }


    public Object internalGet(String key) {
        if (key.equals(ALL_EVENTS)) {
            try {
                EventChannelPair[] ecps = jdbcECS.getAllForStation(stationId);
                // use a set to remove duplicate events
                HashSet out = new HashSet();
                for (int i = 0; i < ecps.length; i++) {
                    out.add(ecps[i].getEvent());
                }
                return out;
            } catch (SQLException e) {
                throw new RuntimeException("can't get for key="+key, e);
            }
        } else if (key.equals(SUCCESS_EVENTS_KEY)) {
            try {
                EventChannelPair[] ecps = jdbcECS.getSuccessfulForStation(stationId);
                // use a set to remove duplicate events
                HashSet out = new HashSet();
                for (int i = 0; i < ecps.length; i++) {
                    out.add(ecps[i].getEvent());
                }
                return out;
            } catch (SQLException e) {
                throw new RuntimeException("can't get for key="+key, e);
            }
        } else if (key.equals(SUCCESS_ECPS_KEY)) {
            try {
                EventChannelPair[] ecps = jdbcECS.getSuccessfulForStation(stationId);

                HashSet out = new HashSet();
                for (int i = 0; i < ecps.length; i++) {
                    out.add(ecps[i]);
                }
                return out;
            } catch (SQLException e) {
                throw new RuntimeException("can't get for key="+key, e);
            }
        } else if (key.equals(SUCCESS_ECGROUP_KEY)) {
            try {
                EventChannelPair[] ecps = jdbcECS.getSuccessfulForStation(stationId);
                HashSet out = new HashSet();
                if (Start.getWaveformArm().getMotionVectorArm() != null) {
                    for (int i = 0; i < ecps.length; i++) {
                        out.add(Start.getWaveformArm().getEventVectorPair(ecps[i]));
                    }
                } else {
                    for (int i = 0; i < ecps.length; i++) {
                        out.add(ecps[i]);
                    }
                }
                return out;
            } catch (SQLException e) {
                throw new RuntimeException("can't get for key="+key, e);
            } catch(NetworkNotFound e) {
                throw new RuntimeException("can't get for key="+key, e);
            }
        } else {
            return super.internalGet(key);
        }
    }

    public boolean internalContainsKey(Object key) {
        if (key.equals(ALL_EVENTS)
            || key.equals(SUCCESS_EVENTS_KEY)
            || key.equals(SUCCESS_ECPS_KEY)
            || key.equals(SUCCESS_ECGROUP_KEY)) {
            return true;
        } else {
            return super.internalContainsKey(key);
        }
    }

    public Object[] internalGetKeys() {
        return new String[] {ALL_EVENTS};
    }

    protected int stationId;

    public static final String ALL_EVENTS = "station_events";

    public static final String SUCCESS_EVENTS_KEY = "successful_station_events";

    public static final String SUCCESS_ECPS_KEY = "successful_event_channels";

    public static final String SUCCESS_ECGROUP_KEY = "successful_event_channel_groups";
}

