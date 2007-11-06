/**
 * NetworkArmContext.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.status.networkArm;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.Start;

public class NetworkArmContext extends AbstractContext {

    public NetworkArmContext() throws SQLException {
        super();
        netDb = new NetworkDB();
    }

    public NetworkArmContext(Context context) throws SQLException {
        super(context);
        netDb = new NetworkDB();
    }

    public Object internalGet(String key) {
        if(key.equals(ALL_NETS_KEY)) {
            CacheNetworkAccess[] netdbs = netDb.getAllNets(Start.getNetworkArm()
                    .getNetworkDC());
            ArrayList out = new ArrayList(netdbs.length);
            for(int i = 0; i < netdbs.length; i++) {
                out.add(netdbs[i]);
            }
            return out;
        } else if(key.equals(SUCCESSFUL_NETS_KEY)) {
            try {
                CacheNetworkAccess[] netdbs = Start.getNetworkArm()
                        .getSuccessfulNetworks();
                ArrayList out = new ArrayList(netdbs.length);
                for(int i = 0; i < netdbs.length; i++) {
                    out.add(netdbs[i]);
                }
                return out;
            } catch(Exception e) {
                throw new RuntimeException("can't get for key=" + key, e);
            }
        } else if(key.length() == 2
                && !(key.startsWith("X") || key.startsWith("Y") || key.startsWith("Z"))) {
            // try as a network code
            CacheNetworkAccess[] netdbs = netDb.getAllNets(Start.getNetworkArm()
                    .getNetworkDC());
            ArrayList out = new ArrayList(netdbs.length);
            for(int i = 0; i < netdbs.length; i++) {
                if(netdbs[i].get_attributes().get_code().equals(key)) {
                    return netdbs[i];
                }
            }
        }
        // else
        return null;
    }

    public boolean internalContainsKey(Object key) {
        if(key.equals(ALL_NETS_KEY)) {
            return true;
        } else {
            return false;
        }
    }

    public Object[] internalGetKeys() {
        return new String[] {ALL_NETS_KEY};
    }

    public Object internalRemove(Object key) {
        throw new RuntimeException("Read only context, operation remove not permitted: key="
                + key);
    }

    public Object internalPut(String key, Object p2) {
        throw new RuntimeException("Read only context, operation put not permitted: key="
                + key);
    }

    NetworkDB netDb;

    public static final String ALL_NETS_KEY = "network_arm_all";

    public static final String SUCCESSFUL_NETS_KEY = "successful_networks";
}
