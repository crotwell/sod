/**
 * StationWaveformContext.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.EventChannelPair;
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
        } else {
            return super.internalGet(key);
        }
    }

    public boolean internalContainsKey(Object key) {
        if (key.equals(ALL_EVENTS) ) {
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
}

