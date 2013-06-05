/**
 * NetworkArmContext.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.status.networkArm;

import java.util.List;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.source.SodSourceException;

public class NetworkArmContext extends AbstractContext {

    public NetworkArmContext()   {
        super();
        netDb =  NetworkDB.getSingleton();
    }

    public NetworkArmContext(Context context)   {
        super(context);
        netDb =  NetworkDB.getSingleton();
    }

    public Object internalGet(String key) {
        if(key.equals(ALL_NETS_KEY)) {
            try {
                return Start.getNetworkArm().getNetworkSource().getNetworks();
            } catch(SodSourceException e) {
                throw new RuntimeException("can't get for key=" + key, e);
            }
        } else if(key.equals(SUCCESSFUL_NETS_KEY)) {
            try {
                return Start.getNetworkArm()
                        .getSuccessfulNetworks();
            } catch(Exception e) {
                throw new RuntimeException("can't get for key=" + key, e);
            }
        } else if(key.length() == 2
                && !(key.startsWith("X") || key.startsWith("Y") || key.startsWith("Z"))) {
            // try as a network code
            List<NetworkAttrImpl> dbAttrs =  netDb.getNetworkByCode(key);
            for (NetworkAttrImpl attr : dbAttrs) {
                if(attr.get_code().equals(key)) {
                    return Start.getNetworkArm().getNetworkSource().getNetwork(attr);
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
