/**
 * WaveformArmContext.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

public class WaveformArmContext  extends AbstractContext {

    public WaveformArmContext() throws SQLException {
        jdbcECS = new JDBCEventChannelStatus();
    }
    public WaveformArmContext(Context context) throws SQLException {
        super(context);
        jdbcECS = new JDBCEventChannelStatus();
    }


    public Object internalGet(String key) {
        if (key.equals(ALL_ECPS_KEY)) {
            try {
                EventChannelPair[] ecps = jdbcECS.getAll();
                ArrayList out = new ArrayList(ecps.length);
                for (int i = 0; i < ecps.length; i++) {
                    out.add(ecps[i]);
                }
                return out;
            } catch (SQLException e) {
                throw new RuntimeException("can't get for key="+key, e);
            }
        } else {
            return null;
        }
    }

    public boolean internalContainsKey(Object key) {
        if (key.equals(ALL_ECPS_KEY) ) {
            return true;
        } else {
            return false;
        }
    }

    public Object[] internalGetKeys() {
        return new String[] {ALL_ECPS_KEY};
    }

    public Object internalRemove(Object key) {
        throw new RuntimeException("Read only context, operation remove not permitted: key="+key);
    }

    public Object internalPut(String key, Object p2) {
        throw new RuntimeException("Read only context, operation put not permitted: key="+key);
    }

    public static final String ALL_ECPS_KEY = "all_ecps";

    JDBCEventChannelStatus jdbcECS;
}

